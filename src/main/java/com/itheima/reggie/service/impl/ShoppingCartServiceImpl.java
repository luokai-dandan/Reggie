package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.ShoppingCartMapper;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    //@Resource
    //private RedisTemplate<String, List<ShoppingCart>> redisTemplate;

    /**
     * 购物车列表
     *
     * @return
     */
    @Override
    public List<ShoppingCart> getList() {

        //动态构造key
        //String key = "shoppingCart_" + BaseContext.getCurrentId(); //dish_

        //先从redis中获取缓存数据
        //shoppingCartList = redisTemplate.opsForValue().get(key);
        //如果存在，直接返回，无需查询数据库
        //if (shoppingCartList != null) {
        //    //如果存在，直接返回，无需查询数据库
        //    return shoppingCartList;
        //}

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        //redisTemplate.opsForValue().set(key, shoppingCartList, 60, TimeUnit.MINUTES);

        return shoppingCartService.list(queryWrapper);
    }

    /**
     * 加购物车
     *
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart addDishToSC(ShoppingCart shoppingCart) {

        //log.info("shoppingCart: {}", shoppingCart);

        // 设置用户id，指定当前是哪个用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 同一菜品重复添加购物车，并不需要在数据库添加多份，而是修改同一菜品份数
        // 查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //添加到购物车的套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart sc = shoppingCartService.getOne(queryWrapper);

        if (sc != null) {
            //如果已经存在，就在原数量基础上加1
            Integer number = sc.getNumber();
            sc.setNumber(number + 1);
            shoppingCartService.updateById(sc);
        } else {
            //如果不存在，则添加到购物车，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //此时cartServiceOne为空，统一处理返回cartServiceOne
            sc = shoppingCart;
        }

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("shoppingCart_*");
        //redisTemplate.delete(keys);

        return sc;
    }

    /**
     * 减购物车
     *
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart subDishToSC(ShoppingCart shoppingCart) {

        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        //代表数量减少的是菜品数量
        if (dishId != null) {
            //通过dishId查出购物车对象
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
            //这里必须要加两个条件，否则会出现用户互相修改对方与自己购物车中相同套餐或者是菜品的数量
            queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
            ShoppingCart cart1 = shoppingCartService.getOne(queryWrapper);
            cart1.setNumber(cart1.getNumber() - 1);
            Integer LatestNumber = cart1.getNumber();
            if (LatestNumber > 0) {
                //对数据进行更新操作
                shoppingCartService.updateById(cart1);
            } else if (LatestNumber == 0) {
                //如果购物车的菜品数量减为0，那么就把菜品从购物车删除
                shoppingCartService.removeById(cart1.getId());
            } else if (LatestNumber < 0) {
                return null;
            }

            return cart1;
        }

        Long setmealId = shoppingCart.getSetmealId();
        if (setmealId != null) {
            //代表是套餐数量减少
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId).eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
            ShoppingCart cart2 = shoppingCartService.getOne(queryWrapper);
            cart2.setNumber(cart2.getNumber() - 1);
            Integer LatestNumber = cart2.getNumber();
            if (LatestNumber > 0) {
                //对数据进行更新操作
                shoppingCartService.updateById(cart2);
            } else if (LatestNumber == 0) {
                //如果购物车的套餐数量减为0，那么就把套餐从购物车删除
                shoppingCartService.removeById(cart2.getId());
            } else if (LatestNumber < 0) {
                return null;
            }
            return cart2;
        }
        //如果两个大if判断都进不去
        return null;


        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("shoppingCart_*");
        //redisTemplate.delete(keys);
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @Override
    public void cleanSC() {

        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        shoppingCartService.remove(queryWrapper);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("shoppingCart_*");
        //redisTemplate.delete(keys);
    }

}

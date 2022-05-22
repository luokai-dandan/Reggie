package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
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

    @Autowired
    private RedisTemplate<Object, Object> redisTemplateShoppingCart;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 购物车列表
     *
     * @return
     */
    @Override
    public List<ShoppingCart> getList() {

        Long userId = (Long) redisTemplate.opsForValue().get("user");
        List<ShoppingCart> shoppingCartList = null;
        //动态构造key
        String key = "shoppingCart_" + userId;
        //先从redis中获取缓存数据
        shoppingCartList = (List<ShoppingCart>) redisTemplateShoppingCart.opsForValue().get(key);
        //如果存在，直接返回，无需查询数据库
        if (shoppingCartList != null) {
            //如果存在，直接返回，无需查询数据库
            return shoppingCartList;
        }
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        shoppingCartList = shoppingCartService.list(queryWrapper);

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        redisTemplateShoppingCart.opsForValue().set(key, shoppingCartList, 60, TimeUnit.MINUTES);

        return shoppingCartList;
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
        //Long userId = BaseContext.getCurrentId();
        Long userId = (Long) redisTemplate.opsForValue().get("user");
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
        Set<Object> keys = redisTemplateShoppingCart.keys("shoppingCart_*");
        redisTemplateShoppingCart.delete(Objects.requireNonNull(keys));

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

        Long userId = (Long) redisTemplate.opsForValue().get("user");

        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        //代表是菜品数量减少
        if (dishId != null) {
            //通过dishId查出购物车对象
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
            //确保是该用户下的购物车
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            //查询购物车中减的该对象
            ShoppingCart cartDish = shoppingCartService.getOne(queryWrapper);
            //数量减一
            cartDish.setNumber(cartDish.getNumber() - 1);
            //减完重新获取该对象的数量
            Integer LatestNumber = cartDish.getNumber();
            //减完数量仍然大于零，更新number字段
            if (LatestNumber > 0) {
                //对数据进行更新操作
                shoppingCartService.updateById(cartDish);
            } else if (LatestNumber == 0) {
                //如果购物车的菜品数量减为0，那么就把菜品从购物车删除
                shoppingCartService.removeById(cartDish.getId());
            } else {
                //数量小于0，抛异常
                throw new CustomException("购物车异常");
            }
            //返回该对象
            return cartDish;
        }

        Long setmealId = shoppingCart.getSetmealId();
        // 代表是套餐数量减少
        if (setmealId != null) {
            //通过setmealId查出购物车对象
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
            //确保是该用户下的购物车
            queryWrapper.eq(ShoppingCart::getUserId, userId);
            //查询购物车中减的该对象
            ShoppingCart cartSetmeal = shoppingCartService.getOne(queryWrapper);
            //数量减一
            cartSetmeal.setNumber(cartSetmeal.getNumber() - 1);
            //减完重新获取该对象的数量
            Integer LatestNumber = cartSetmeal.getNumber();
            //减完数量仍然大于零，更新number字段
            if (LatestNumber > 0) {
                //对数据进行更新操作
                shoppingCartService.updateById(cartSetmeal);
            } else if (LatestNumber == 0) {
                //如果购物车的套餐数量减为0，那么就把套餐从购物车删除
                shoppingCartService.removeById(cartSetmeal.getId());
            } else {
                //数量小于0，抛异常
                throw new CustomException("购物车异常");
            }
            //返回该对象
            return cartSetmeal;
        }
        //清理所有菜品的缓存数据
        Set<Object> keys = redisTemplateShoppingCart.keys("shoppingCart_*");
        redisTemplateShoppingCart.delete(Objects.requireNonNull(keys));

        //既不是菜品又不是套餐，直接返回
        return null;
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @Override
    public void cleanSC() {

        //Long userId = BaseContext.getCurrentId();
        Long userId = (Long) redisTemplate.opsForValue().get("user");
        //log.info("BaseContext.getCurrentId()：{}", userId);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        shoppingCartService.remove(queryWrapper);

        //清理所有菜品的缓存数据
        Set<Object> keys = redisTemplateShoppingCart.keys("shoppingCart_*");
        redisTemplateShoppingCart.delete(Objects.requireNonNull(keys));
    }

}

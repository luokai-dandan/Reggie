package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车业务功能
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
@Api(tags = "购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart: {}", shoppingCart);

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
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne!=null) {
            //如果已经存在，就在原数量基础上加1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //如果不存在，则添加到购物车，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //此时cartServiceOne为空，统一处理返回cartServiceOne
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart: {}", shoppingCart);

        // 设置用户id，指定当前是哪个用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 同一菜品重复添加购物车，并不需要在数据库添加多份，而是修改同一菜品份数
        // 查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        if (dishId != null) {
            //从购物车删除的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //从购物车删除的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
        Integer count = shoppingCartOne.getNumber();

        if (count > 1) {
            //存在多份，就在原数量基础上减1
            shoppingCartOne.setNumber(count-1);
            shoppingCartService.updateById(shoppingCartOne);
        } else {
            //如果存在一份，则删除
            shoppingCartService.removeById(shoppingCartOne);
        }
        return R.success(shoppingCartOne);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        return R.success(shoppingCartList);
    }

    /**
     * 根据用户id删除购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> delete(){
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        shoppingCartService.remove(queryWrapper);

        return R.success("删除成功");
    }
}

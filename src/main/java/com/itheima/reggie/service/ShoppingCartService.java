package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 购物车列表
     *
     * @return
     */
    public List<ShoppingCart> getList();

    /**
     * 加购物车
     *
     * @param shoppingCart
     * @return
     */
    public ShoppingCart addDishToSC(ShoppingCart shoppingCart);

    /**
     * 减购物车
     *
     * @param shoppingCart
     * @return
     */
    public ShoppingCart subDishToSC(ShoppingCart shoppingCart);

    /**
     * 清空购物车
     *
     * @return
     */
    public void cleanSC();
}

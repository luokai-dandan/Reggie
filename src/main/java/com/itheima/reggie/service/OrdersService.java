package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.QueryPageDate;
import com.itheima.reggie.mongo.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     *
     * @param order
     */
    public void submitOrders(Orders order);


    /**
     * 手机端通过mongodb查看订单信息
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<OrdersDto> getUserPage(int page, int pageSize);

    /**
     * mongo分页查询订单信息（管理端）
     *
     * @param queryPageDate
     * @return
     */
    public Page<Order> getPage(QueryPageDate queryPageDate);

    /**
     * 修改订单状态
     *
     * @param orders
     * @return
     */
    public void updateOrdersStatus(Orders orders);

    /**
     * 再来一单
     *
     * @param map
     */
    public void againOrders(Map<String, String> map);
}

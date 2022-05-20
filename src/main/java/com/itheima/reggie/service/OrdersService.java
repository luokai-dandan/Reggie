package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.QueryPageDate;

import java.util.List;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     * @param order
     */
    public void submit(Orders order);

    /**
     * 手机端查看订单及订单详情
     * @param queryPageDate
     * @return
     */
    public List<OrdersDto> getOrdersAndDetail(QueryPageDate queryPageDate);
}

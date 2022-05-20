package com.itheima.reggie.controller;

import com.itheima.reggie.service.OrdersDetailService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/orderDetail")
@Api(tags = "订单详情接口")
public class OrdersDetailController {

    @Autowired
    private OrdersDetailService ordersDetailService;

}

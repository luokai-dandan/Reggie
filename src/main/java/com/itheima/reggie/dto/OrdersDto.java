package com.itheima.reggie.dto;

import com.itheima.reggie.entity.OrdersDetail;
import com.itheima.reggie.entity.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 订单加订单详情包装类
 */
@Data
@ApiModel("订单包装类")
public class OrdersDto extends Orders {

    @ApiModelProperty("订单详情")
    private List<OrdersDetail> ordersDetails;
}
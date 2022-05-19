package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细
 */
@Data
@ApiModel("订单详情")
public class OrderDetail implements Serializable {

    @ApiModelProperty("序列化版本号")
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    //名称
    @ApiModelProperty("名称")
    private String name;

    //订单id
    @ApiModelProperty("订单编号")
    private Long orderId;

    //菜品id
    @ApiModelProperty("菜品编号")
    private Long dishId;

    //套餐id
    @ApiModelProperty("套餐编号")
    private Long setmealId;

    //口味
    @ApiModelProperty("菜品口味")
    private String dishFlavor;

    //数量
    @ApiModelProperty("菜品数量")
    private Integer number;

    //金额
    @ApiModelProperty("菜品金额")
    private BigDecimal amount;

    //图片
    @ApiModelProperty("菜品图片名")
    private String image;
}

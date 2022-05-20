package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
@ApiModel("购物车类")
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    //名称
    @ApiModelProperty("菜品名称")
    private String name;

    //用户id
    @ApiModelProperty("用户编号")
    private Long userId;

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
    @ApiModelProperty("菜品价格")
    private BigDecimal amount;

    //图片
    @ApiModelProperty("菜品图片")
    private String image;

    @ApiModelProperty("菜品加入购物车时间")
    private LocalDateTime createTime;
}

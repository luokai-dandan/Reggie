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
 * 套餐菜品关系
 */
@Data
@ApiModel("套餐菜品类")
public class SetmealDish implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    //套餐id
    @ApiModelProperty("套餐编号")
    private Long setmealId;

    //菜品id
    @ApiModelProperty("菜品编号")
    private Long dishId;

    //菜品名称 （冗余字段）
    @ApiModelProperty("菜品名称")
    private String name;

    //菜品原价
    @ApiModelProperty("菜品价格")
    private BigDecimal price;

    //份数
    @ApiModelProperty("菜品份数")
    private Integer copies;

    //排序
    @ApiModelProperty("顺序")
    private Integer sort;

    //@ApiModelProperty("套餐菜品创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //@ApiModelProperty("套餐菜品更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //@ApiModelProperty("套餐菜品创建人")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    //@ApiModelProperty("套餐菜品修改人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    //是否删除
    @ApiModelProperty("是否删除")
    private Integer isDeleted;
}

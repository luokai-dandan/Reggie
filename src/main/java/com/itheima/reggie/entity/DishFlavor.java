package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
菜品口味
 */
@Data
@ApiModel("菜品口味类")
public class DishFlavor implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    //菜品id
    @ApiModelProperty("菜品编号")
    private Long dishId;

    //口味名称
    @ApiModelProperty("菜品口味名称")
    private String name;

    //口味列表
    @ApiModelProperty("菜品口味列表")
    private String value;

    //@ApiModelProperty("菜品创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //@ApiModelProperty("菜品更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //@ApiModelProperty("菜品创建人")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    //@ApiModelProperty("菜品修改人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    //是否删除
    @ApiModelProperty("是否删除")
    private Integer isDeleted;

}

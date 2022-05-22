package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 地址簿
 */
@Data
@ApiModel("地址类")
public class AddressBook implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    //用户id
    @ApiModelProperty("用户编号")
    private Long userId;

    //收货人
    @ApiModelProperty("收货人")
    private String consignee;

    //手机号
    @ApiModelProperty("收货人手机号")
    private String phone;

    //性别 0 女 1 男
    @ApiModelProperty("收货人性别{0:女,1:男}")
    private String sex;

    //省级区划编号
    @ApiModelProperty("省级区划编号")
    private String provinceCode;

    //省级名称
    @ApiModelProperty("省级名称")
    private String provinceName;

    //市级区划编号
    @ApiModelProperty("市级区划编号")
    private String cityCode;

    //市级名称
    @ApiModelProperty("市级名称")
    private String cityName;

    //区级区划编号
    @ApiModelProperty("区级区划编号")
    private String districtCode;

    //区级名称
    @ApiModelProperty("区级名称")
    private String districtName;

    //详细地址
    @ApiModelProperty("详细地址")
    private String detail;

    //标签
    @ApiModelProperty("地址标签")
    private String label;

    //是否默认 0 否 1是
    @ApiModelProperty("是否为默认地址:{0:否,1:是}")
    private Integer isDefault;

    //创建时间
    @ApiModelProperty("地址创建时间")
    private LocalDateTime createTime;

    //更新时间
    @ApiModelProperty("套餐菜品更新时间")
    private LocalDateTime updateTime;

    //创建人
    @ApiModelProperty("套餐菜品创建人")
    private Long createUser;

    //修改人
    @ApiModelProperty("套餐菜品修改人")
    private Long updateUser;

    //是否删除
    @ApiModelProperty("是否删除")
    private Integer isDeleted;
}

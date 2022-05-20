package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * 员工实体
 * */
@Data
@ApiModel("员工类")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("员工用户名")
    private String username;

    @ApiModelProperty("员工姓名")
    private String name;

    @ApiModelProperty("加密后的密码")
    private String password;

    @ApiModelProperty("员工手机号")
    private String phone;

    @ApiModelProperty("员工性别")
    private String sex;

    @ApiModelProperty("员工身份证号码")
    private String idNumber;//身份证号码

    @ApiModelProperty("售卖状态{0:停售,1:起售}")
    private Integer status;

    //@ApiModelProperty("员工信息创建时间")
    @TableField(fill = FieldFill.INSERT) //插入时自动填充
    private LocalDateTime createTime;

    //@ApiModelProperty("员工信息更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时自动填充
    private LocalDateTime updateTime;

    //@ApiModelProperty("员工信息创建人")
    @TableField(fill = FieldFill.INSERT) //插入时自动填充
    private Long createUser;

    //@ApiModelProperty("员工信息修改人")
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时自动填充
    private Long updateUser;

}

package com.itheima.reggie.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
/**
 * 用户信息
 */
@Data
@ApiModel("用户信息")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    //姓名
    @ApiModelProperty("用户名")
    private String name;

    //手机号
    @ApiModelProperty("用户手机号")
    private String phone;

    //性别 0 女 1 男
    @ApiModelProperty("用户性别")
    private String sex;

    //身份证号
    @ApiModelProperty("用户身份证号")
    private String idNumber;

    //头像
    @ApiModelProperty("用户头像")
    private String avatar;

    //状态 0:禁用，1:正常
    @ApiModelProperty("用户状态:{0:禁用,1:正常}")
    private Integer status;
}

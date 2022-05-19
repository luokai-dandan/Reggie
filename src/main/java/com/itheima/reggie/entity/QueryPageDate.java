package com.itheima.reggie.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("订单分页参数封装")
public class QueryPageDate implements Serializable {

    @ApiModelProperty("序列化版本号")
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("页码")
    private int page;

    @ApiModelProperty("每页记录数")
    private int pageSize;

    @ApiModelProperty("每页记录数")
    private String number;

    @ApiModelProperty("开始日期格式转换")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    @ApiModelProperty("结束日期格式转换")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}

package com.itheima.reggie.common;

import io.swagger.annotations.*;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/*
 * 通用返回结果，服务端响应的数据最终都会封装成此对象
 * */

@Data
@ApiModel("返回结果")
public class R<T> implements Serializable {

    @ApiModelProperty("编码")
    private Integer code; //编码：1成功，0和其它数字为失败

    @ApiModelProperty("错误信息")
    private String msg; //错误信息

    @ApiModelProperty("数据")
    private T data; //数据

    @ApiModelProperty("动态数据")
    private Map map = new HashMap(); //动态数据

    @ApiOperation(value = "返回成功结果接口")
    //@ApiImplicitParam(name = "object", value = "返回数据")
    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    @ApiOperation(value = "返回失败结果接口")
    //@ApiImplicitParam(name = "object", value = "返回数据")
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    @ApiOperation(value = "返回添加数据接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "新增数据键", required = true),
            @ApiImplicitParam(name = "value", value = "新增数据值", required = true)
    })
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}

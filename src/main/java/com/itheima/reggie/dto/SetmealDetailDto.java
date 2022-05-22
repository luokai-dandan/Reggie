package com.itheima.reggie.dto;

import com.itheima.reggie.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("套餐详情包装类")
public class SetmealDetailDto extends SetmealDish {

    //菜品图片
    @ApiModelProperty("菜品图片")
    private String image;

    //描述信息
    @ApiModelProperty("菜品描述信息")
    private String description;

}

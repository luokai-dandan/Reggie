package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("菜品包装类")
public class DishDto extends Dish {

    @ApiModelProperty("口味数据")
    private List<DishFlavor> flavors = new ArrayList<>();

    @ApiModelProperty("菜品分类名")
    private String categoryName;

    @ApiModelProperty("菜品份数")
    private Integer copies;
}

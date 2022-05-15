package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时保存套餐关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 根据id查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id);

    //修改套餐
    public void updateWithDish(SetmealDto setmealDto);

    //删除套餐及关联的菜品
    public void deleteWithDish(List<Long> ids);
}

package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 根据条件查询对应的菜品数据及分类信息和移动端的口味数据
     *
     * @param dish
     * @return
     */
    public List<DishDto> getList(Dish dish);

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    public Page<DishDto> getPage(int page, int pageSize, String name);

    /**
     * 新增菜品，同时插入菜品对应的口味数据：dish，dish_flavor
     *
     * @param dishDto
     * @return
     */
    public Boolean addDishWithFlavor(DishDto dishDto);

    /**
     * 修改菜品表及口味表
     *
     * @param dishDto
     */
    public Boolean updateDishWithFlavor(DishDto dishDto);

    /**
     * 删除菜品及口味表
     *
     * @param ids
     */
    public Boolean deleteDishWithFlavor(List<Long> ids);

    /**
     * 修改菜品状态
     *
     * @param ids
     * @param status
     * @return
     */
    public Boolean updateDishStatus(List<Long> ids, Integer status);

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    public DishDto getByIdDishWithFlavor(Long id);

}

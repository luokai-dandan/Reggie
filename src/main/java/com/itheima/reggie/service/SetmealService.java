package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 查询套餐列表
     * @param setmeal
     * @return
     */
    public List<Setmeal> getList(Setmeal setmeal);

    /**
     * 套餐信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    public Page<SetmealDto> getPage(int page, int pageSize, String name);

    /**
     * 新增套餐，同时保存套餐关联关系
     * @param setmealDto
     */
    public Boolean addSetmealWithDish(SetmealDto setmealDto);

    /**
     * 根据id查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    public SetmealDto getByIdSetmealWithDish(Long id);

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    public Boolean updateSetmealWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐及关联的菜品
     * @param ids
     * @return
     */
    public Boolean deleteSetmealWithDish(List<Long> ids);

    /**
     * 更新套餐状态
     * @param status
     * @param ids
     * @return
     */
    public Boolean updateStatusByIds(List<Long> ids, Integer status);
}

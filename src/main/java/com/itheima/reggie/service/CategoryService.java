package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 手机端获取分类列表
     *
     * @param category
     * @return
     */
    public List<Category> getList(Category category);

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<Category> getPage(int page, int pageSize);

    /**
     * 新增分类
     *
     * @param category
     * @return
     */
    public Boolean addCategory(Category category);

    /**
     * 修改分类
     *
     * @param category
     * @return
     */
    public Boolean updateCategory(Category category);

    /**
     * 新增分类
     *
     * @param ids
     * @return
     */
    public Boolean deleteCategoryById(Long ids);

}

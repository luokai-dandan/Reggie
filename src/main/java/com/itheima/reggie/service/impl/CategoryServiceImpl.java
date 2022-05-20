package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     *
     * @param category
     * @return
     */
    @Override
    public List<Category> getList(Category category) {

        //log.info("category: {}",category.toString());
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        return categoryService.list(queryWrapper);

    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page<Category> getPage(int page, int pageSize) {

        //log.info("page = {}, pageSize = {}", page, pageSize);

        //构造分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //执行查询
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo, queryWrapper);

        return pageInfo;
    }

    /**
     * 新增分类
     *
     * @param category
     * @return
     */
    @Override
    public Boolean addCategory(Category category) {

        //log.info("category: {}", category.toString());
        return categoryService.save(category);
    }

    /**
     * 分类信息修改
     *
     * @param category
     * @return
     */
    @Override
    public Boolean updateCategory(Category category) {

        //log.info("employee:{}", category.toString());
        return categoryService.updateById(category);
    }


    /**
     * 根据id删除分类，删除之前需要进行判断
     *
     * @param ids
     */
    @Override
    public Boolean deleteCategoryById(Long ids) {

        //初始化构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据ids进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if (count1 > 0) {
            //已经关联菜品，抛出异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //初始化构造器
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据ids进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        if (count2 > 0) {
            //已经关联套餐，抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //正常删除分类
        return super.removeById(ids);
    }
}

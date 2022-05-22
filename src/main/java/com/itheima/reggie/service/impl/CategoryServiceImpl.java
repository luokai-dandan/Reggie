package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
     * 手机端获取分类列表
     *
     * @param category
     * @return
     */
    @Override
    public List<Category> getList(Category category) {

        //log.info("category: {}",category.toString());
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件（添加菜品和添加套餐时选择下拉分类列表）
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //查询未删除的分类
        queryWrapper.eq(Category::getIsDeleted, 0);
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
        //查询未删除的分类
        queryWrapper.eq(Category::getIsDeleted, 0);
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
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public Boolean addCategory(Category category) {

        //先查看数据库中是否存在删除后的该分类
        LambdaQueryWrapper<Category> queryWrapperSameNameDeleted = new LambdaQueryWrapper<>();
        queryWrapperSameNameDeleted.eq(Category::getName, category.getName());
        int count = categoryService.count(queryWrapperSameNameDeleted);
        if (count==1) {
            //存在同名删除后的分类，删除已存在的数据，重新插入新的数据
            //因为name为索引字段，具有唯一性，用name来取id
            Category categoryDeleted = categoryService.getOne(queryWrapperSameNameDeleted);
            //彻底移除
            categoryService.removeById(categoryDeleted.getId());
        }
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
        //先查看数据库中是否存在删除后的该分类
        LambdaQueryWrapper<Category> queryWrapperSameNameDeleted = new LambdaQueryWrapper<>();
        queryWrapperSameNameDeleted.eq(Category::getName, category.getName());
        queryWrapperSameNameDeleted.eq(Category::getIsDeleted, 1);
        int count = categoryService.count(queryWrapperSameNameDeleted);
        if (count==1) {
            //存在同名删除后的分类，删除已存在的数据，重新插入新的数据
            //因为name为索引字段，具有唯一性，用name来取id
            Category categoryDeleted = categoryService.getOne(queryWrapperSameNameDeleted);
            //彻底移除
            categoryService.removeById(categoryDeleted.getId());
        }
        return categoryService.updateById(category);
    }


    /**
     * 根据id删除分类，删除之前需要进行判断
     *
     * @param ids
     */
    @Override
    public Boolean deleteCategoryById(Long ids) {

        // 初始化构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据ids进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        // 查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if (count1 > 0) {
            //已经关联菜品，抛出异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 初始化构造器
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据ids进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        if (count2 > 0) {
            //已经关联套餐，抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        // 正常删除分类，伪删除，将is_delete改为1
        LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(null != ids, Category::getId, ids);
        updateWrapper.set(Category::getIsDeleted, 1);

        return categoryService.update(updateWrapper);
    }
}

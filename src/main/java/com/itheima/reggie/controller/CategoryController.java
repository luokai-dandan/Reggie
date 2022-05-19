package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 分离管理
 */
@Slf4j
@RestController
@RequestMapping("/category")
@Api(tags = "菜品和套餐分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @ApiOperation(value = "分类新增接口")
    @ApiImplicitParam(name = "category", value = "分类实体")
    public R<String> add(@RequestBody Category category){
        log.info("category: {}", category.toString());
        categoryService.save(category);

        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "分类分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true)
    })
    public R<Page<Category>> page(int page, int pageSize){
        log.info("page = {}, pageSize = {}", page, pageSize);

        //构造分页构造器
        Page<Category> pageInfo = new Page<Category>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //执行查询
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 分类信息修改
     * @param category
     * @return
     */
    @PutMapping
    @ApiOperation(value = "分类修改接口")
    @ApiImplicitParam(name = "category", value = "分类实体")
    public R<String> update(@RequestBody Category category){
        log.info("employee:{}", category.toString());

        categoryService.updateById(category);
        return R.success("分类信息修改成功");
    }

    /**
     * 根据id删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "分类删除接口")
    @ApiImplicitParam(name = "ids", value = "分类编号")
    public R<String> delete(Long ids){

        log.info("删除分类，id为：{}", ids);
        // categoryService.removeById(ids);
        categoryService.remove(ids);

        return R.success("分类信息删除成功");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "分类列表查询接口")
    @ApiImplicitParam(name = "category", value = "分类实体")
    public R<List<Category>>list(Category category){

        log.info("category: {}",category.toString());
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType()!=null, Category::getType, category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }
}

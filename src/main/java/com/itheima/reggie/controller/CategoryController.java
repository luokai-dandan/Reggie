package com.itheima.reggie.controller;

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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    /**
     * 根据条件查询分类数据
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "分类列表查询接口")
    @ApiImplicitParam(name = "category", value = "分类实体")
    @Cacheable(value = "categoryCache", key = "#category.id + '_' + #category.type")
    public R<List<Category>> list(Category category) {

        List<Category> categoryList = categoryService.getList(category);
        return categoryList != null ? R.success(categoryList) : R.error("查询错误");
    }

    /**
     * 分页查询
     *
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
    public R<Page<Category>> page(int page, int pageSize) {

        Page<Category> categoryPage = categoryService.getPage(page, pageSize);
        return categoryPage != null ? R.success(categoryPage) : R.error("查询错误");
    }

    /**
     * 新增分类
     *
     * @param category
     * @return
     */
    @PostMapping
    @ApiOperation(value = "分类新增接口")
    @ApiImplicitParam(name = "category", value = "分类实体")
    @CacheEvict(value = "categoryCache", allEntries = true)
    public R<String> add(@RequestBody Category category) {

        Boolean addCategory = categoryService.addCategory(category);
        return addCategory != null ? R.success("新增分类成功") : R.error("新增分类失败");
    }

    /**
     * 分类信息修改
     *
     * @param category
     * @return
     */
    @PutMapping
    @ApiOperation(value = "分类修改接口")
    @ApiImplicitParam(name = "category", value = "分类实体")
    @CacheEvict(value = "categoryCache", allEntries = true)
    public R<String> update(@RequestBody Category category) {

        Boolean update = categoryService.updateCategory(category);
        return update ? R.success("分类信息修改成功") : R.error("分类信息修改失败");
    }

    /**
     * 根据id删除分类
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "分类删除接口")
    @ApiImplicitParam(name = "ids", value = "分类编号")
    @CacheEvict(value = "categoryCache", allEntries = true)
    public R<String> delete(Long ids) {

        //log.info("删除分类，id为：{}", ids);
        Boolean delete = categoryService.deleteCategoryById(ids);
        return delete ? R.success("分类信息删除成功") : R.error("分类信息删除失败");
    }

}

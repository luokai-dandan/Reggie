package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 查询套餐列表
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询套餐列表接口")
    //@ApiImplicitParam(name = "setmeal", value = "套餐列表")
    @Cacheable(value = "setmealCache",  key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){

        List<Setmeal> setmealList = setmealService.getList(setmeal);
        return setmealList!=null?R.success(setmealList):R.error("查询错误");
    }

    /**
     * 套餐信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "套餐分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true),
            @ApiImplicitParam(name = "name", value = "套餐名称", required = false)
    })
    public R<Page<SetmealDto>> page(int page, int pageSize, String name){

        Page<SetmealDto> setmealDtoPage = setmealService.getPage(page, pageSize, name);
        return setmealDtoPage!=null?R.success(setmealDtoPage):R.error("查询错误");
    }

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增套餐接口")
    //@ApiImplicitParam(name = "setmealDto", value = "套餐包装实体")
    //删除setmealCache分类下的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> add(@RequestBody SetmealDto setmealDto){

        Boolean add = setmealService.addSetmealWithDish(setmealDto);
        return add?R.success("添加套餐成功"):R.error("添加套餐失败");
    }


    /**
     * 根据id查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "查询套餐接口")
    //@ApiImplicitParam(name = "id", value = "编号")
    @Cacheable(value = "setmealCache",key = "#id", unless = "#result == null")
    public R<SetmealDto> get(@PathVariable Long id){

        SetmealDto setmealDto = setmealService.getByIdSetmealWithDish(id);
        return setmealDto!=null?R.success(setmealDto):R.error("查询错误");
    }

    /**
     * 修改菜品
     * @param setmealDto
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改套餐接口")
    //@ApiImplicitParam(name = "setmealDto", value = "套餐包装类")
    //删除setmealCache分类下的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){

        Boolean update = setmealService.updateSetmealWithDish(setmealDto);
        return update?R.success("修改菜品成功"):R.error("修改菜品失败");
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "删除套餐接口")
    //@ApiImplicitParam(name = "ids", value = "编号数组")
    //删除setmealCache分类下的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){

        Boolean delete = setmealService.deleteSetmealWithDish(ids);
        return delete?R.success("菜品删除成功"):R.error("菜品删除失败");
    }

    /**
     * 修改套餐状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "修改套餐售卖状态接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "编号数组", required = true),
            @ApiImplicitParam(name = "status", value = "售卖状态", required = true)
    })
    //删除setmealCache分类下的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> updateStatus(@RequestParam("ids") List<Long> ids, @PathVariable Integer status){

        Boolean update = setmealService.updateStatusByIds(ids, status);
        return update?R.success("套餐状态修改成功"):R.error("套餐状态修改失败");
    }

}

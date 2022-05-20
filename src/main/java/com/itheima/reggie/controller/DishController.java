package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 * 使用redisTemplate缓存
 */
@Slf4j
@RestController
@RequestMapping("/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 根据条件查询对应的菜品数据及分类信息和移动端的口味数据
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "菜品列表接口")
    @ApiImplicitParam(name = "dish", value = "菜品实体")
    @Cacheable(value = "dishCache", key = "#dish.categoryId + '_' + #dish.status")
    public R<Object> list(Dish dish) {

        List<DishDto> dishDtoList = dishService.getList(dish);
        return dishDtoList != null ? R.success(dishDtoList) : R.error("查询失败");
    }

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "菜品分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true),
            @ApiImplicitParam(name = "name", value = "菜品名称", required = false)
    })
    public R<Page<DishDto>> page(int page, int pageSize, String name) {

        Page<DishDto> dishDtoPage = dishService.getPage(page, pageSize, name);
        return dishDtoPage!=null?R.success(dishDtoPage):R.error("查询错误");
    }

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增菜品接口")
    @ApiImplicitParam(name = "dishDto", value = "菜品包装类实体")
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> save(@RequestBody DishDto dishDto) {

        Boolean addDishWithFlavor = dishService.addDishWithFlavor(dishDto);
        return addDishWithFlavor ? R.success("添加菜品成功") : R.error("添加菜品失败");
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    @ApiOperation(value = "菜品修改接口")
    @ApiImplicitParam(name = "dishDto", value = "菜品包装类实体")
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> update(@RequestBody DishDto dishDto) {

        Boolean update = dishService.updateDishWithFlavor(dishDto);
        return update ? R.success("修改菜品成功") : R.error("修改菜品失败");
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "菜品删除接口")
    @ApiImplicitParam(name = "ids", value = "菜品编号列表")
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {

        Boolean delete = dishService.deleteDishWithFlavor(ids);
        return delete ? R.success("菜品删除成功") : R.error("菜品删除成功");
    }

    /**
     * 修改菜品状态
     *
     * @param ids
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "菜品售卖状态修改接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "菜品售卖状态", required = true),
            @ApiImplicitParam(name = "ids", value = "菜品编号列表", required = true)
    })
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> updateStatus(@RequestParam("ids") List<Long> ids, @PathVariable Integer status) {

        Boolean updateDishStatus = dishService.updateDishStatus(ids, status);
        return updateDishStatus ? R.success("菜品状态修改成功") : R.error("菜品状态修改失败");
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "查询菜品接口")
    @ApiImplicitParam(name = "id", value = "菜品编号")
    public R<DishDto> get(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdDishWithFlavor(id);
        return dishDto!=null?R.success(dishDto):R.error("查询错误");
    }

}

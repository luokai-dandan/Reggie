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

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    //删除setmealCache分类下的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    @ApiOperation(value = "新增套餐接口")
    @ApiImplicitParam(name = "setmealDto", value = "套餐包装实体")
    public R<String> save(@RequestBody SetmealDto setmealDto){

        log.info("dishDto: {}",setmealDto);

        setmealService.saveWithDish(setmealDto);
        return R.success("添加套餐成功");
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
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //执行查询
        // 第一个参数为函数执行条件，只有name不为空才会执行下面代码，查询字段名为Dish::getName，参数为name的数据
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            //将setmealDto除了categoryName属性外的属性通过item拷贝过来
            BeanUtils.copyProperties(item, setmealDto);
            //获取item属性的categoryId
            Long categoryId = item.getCategoryId(); //分类id
            //根据id查询分类对象及对象中name属性
            Category category = categoryService.getById(categoryId);
            //查到了再做以下操作
            if (category!=null) {
                String categoryName = category.getName();
                //将name赋值到setmealDto对象中，setmealDto其他属性通过上面的BeanUtils.copyProperties拷贝得到
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 根据id查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "查询套餐接口")
    @ApiImplicitParam(name = "id", value = "编号")
    public R<SetmealDto> get(@PathVariable Long id){
        log.info("id: {}",id);

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return R.success(setmealDto);
    }

    /**
     * 修改菜品
     * @param setmealDto
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改套餐接口")
    @ApiImplicitParam(name = "setmealDto", value = "套餐包装类")
    public R<String> update(@RequestBody SetmealDto setmealDto){

        log.info("dishDto: {}",setmealDto);
        setmealService.updateWithDish(setmealDto);
        return R.success("修改菜品成功");
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "删除套餐接口")
    @ApiImplicitParam(name = "ids", value = "编号数组")
    //删除setmealCache分类下的所有缓存数据
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){

        setmealService.deleteWithDish(ids);
        return R.success("菜品删除成功");
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
            @ApiImplicitParam(name = "status", value = "售卖状态", required = true),
            @ApiImplicitParam(name = "ids", value = "编号数组", required = true)
    })
    public R<String> updateStatus(@PathVariable Integer status,@RequestParam("ids") List<Long> ids){
        if (ids == null) {
            throw new CustomException("状态修改异常");
        }
        Setmeal setmeal = new Setmeal();

        for (Long id : ids) {
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("套餐状态修改成功");
    }

    /**
     * 查询套餐列表
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询套餐列表接口")
    @ApiImplicitParam(name = "setmeal", value = "套餐列表")
    @Cacheable(value = "setmealCache",  key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){

        //构造查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null, Setmeal::getCategoryId, setmeal.getCategoryId());
        //添加条件，查询状态为1的菜品
        queryWrapper.eq(setmeal.getStatus()!=null, Setmeal::getStatus,setmeal.getStatus());
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

}

package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 查询套餐列表
     *
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> getList(Setmeal setmeal) {

        //构造查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        //添加条件，查询状态为1的菜品
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        return setmealService.list(queryWrapper);
    }

    /**
     * 套餐信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<SetmealDto> getPage(int page, int pageSize, String name) {

        //log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

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

        List<SetmealDto> setmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //将setmealDto除了categoryName属性外的属性通过item拷贝过来
            BeanUtils.copyProperties(item, setmealDto);
            //获取item属性的categoryId
            Long categoryId = item.getCategoryId(); //分类id
            //根据id查询分类对象及对象中name属性
            Category category = categoryService.getById(categoryId);
            //查到了再做以下操作
            if (category != null) {
                String categoryName = category.getName();
                //将name赋值到setmealDto对象中，setmealDto其他属性通过上面的BeanUtils.copyProperties拷贝得到
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);

        return setmealDtoPage;
    }

    /**
     * 新增套餐，同时保存套餐关联关系
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public Boolean addSetmealWithDish(SetmealDto setmealDto) {

        //log.info("dishDto: {}",setmealDto);

        //从套餐封装类中分离出套餐包含的属性
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto, setmeal, "setmealDishes", "categoryName");

        //生成ID
        Long setmealId = IdWorker.getId();
        setmeal.setId(setmealId);

        //保存套餐基本信息
        this.save(setmeal);

        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        setmealDishList = setmealDishList.stream().peek((item) -> {
            item.setSetmealId(setmealId);
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        return setmealDishService.saveBatch(setmealDishList);
    }

    /**
     * 根据id查询套餐信息和对应的菜品信息
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdSetmealWithDish(Long id) {

        //log.info("id: {}",id);

        //从dish表中查询菜品基本信息
        Setmeal setmeal = this.getById(id);

        //返回前端需要的类
        SetmealDto setmealDto = new SetmealDto();
        //拷贝菜品的普通属性
        BeanUtils.copyProperties(setmeal, setmealDto);

        //查询当前菜品对应的口味信息，从dish_flavor表中查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);

        //加上口味属性赋值
        setmealDto.setSetmealDishes(dishes);

        return setmealDto;
    }

    /**
     * 修改套餐
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public Boolean updateSetmealWithDish(SetmealDto setmealDto) {

        //log.info("dishDto: {}",setmealDto);

        //从套餐封装类中分离出套餐包含的属性
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto, setmeal, "setmealDishes", "categoryName");

        //更新dish表的基本信息
        this.updateById(setmeal);

        //清理当前菜品对应的口味数据--dish_flaovr表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().peek((item) -> {
            item.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList());

        return setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐及关联菜品
     *
     * @param ids
     */
    @Override
    @Transactional
    public Boolean deleteSetmealWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确认是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);

        //如果不能删除，抛出一个业务异常
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据--setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        //删除关系表中的数据---setmeal dish
        return setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 更新套餐状态
     *
     * @param status
     * @param ids
     * @return
     */
    @Override
    public Boolean updateStatusByIds(List<Long> ids, Integer status) {

        if (ids.size() == 0) {
            throw new CustomException("状态修改异常");
        }

        ArrayList<Setmeal> setmealList = new ArrayList<>();

        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmealList.add(setmeal);
        }

        return setmealService.updateBatchById(setmealList);
    }
}

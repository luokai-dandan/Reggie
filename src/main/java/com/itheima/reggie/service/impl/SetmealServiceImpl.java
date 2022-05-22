package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDetailDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private DishService dishService;

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
        //查询未删除的套餐
        queryWrapper.eq(Setmeal::getIsDeleted, 0);
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
        //查询未删除的套餐
        queryWrapper.eq(Setmeal::getIsDeleted, 0);
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
     * 手机端查看套餐详情具体包含菜品
     * @param id
     */
    @Override
    public List<SetmealDetailDto> getDishById(Long id) {
        //log.info("id: {}", id);

        //查询套餐id对应的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null!=id, SetmealDish::getSetmealId,id);
        queryWrapper.eq(SetmealDish::getIsDeleted, 0);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        //初始化前端需要的类
        List<SetmealDetailDto> setmealDetailDtoList = new ArrayList<>();

        for (SetmealDish setmealDish: setmealDishList) {

            SetmealDetailDto setmealDetailDto = new SetmealDetailDto();
            BeanUtils.copyProperties(setmealDish, setmealDetailDto);
            setmealDetailDtoList.add(setmealDetailDto);
        }

        for (SetmealDetailDto setmealDetailDto: setmealDetailDtoList){

            //查询dish表获取菜品图片路径
            LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(null!=setmealDetailDto.getDishId(), Dish::getId, setmealDetailDto.getDishId());

            //补全前端需要的参数（SetmealDish + Dish表中的图片和描述）
            Dish one = dishService.getOne(wrapper);
            setmealDetailDto.setImage(one.getImage());
            setmealDetailDto.setDescription(one.getDescription());
        }

        return setmealDetailDtoList;
    }

    /**
     * 新增套餐，同时保存套餐关联关系
     *
     * @param setmealDto
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public Boolean addSetmealWithDish(SetmealDto setmealDto) {

        //log.info("dishDto: {}",setmealDto);

        //更新前判断名字是否和已删除的套餐名差生冲突
        LambdaQueryWrapper<Setmeal> queryWrapperSameNameDeleted = new LambdaQueryWrapper<>();
        queryWrapperSameNameDeleted.eq(Setmeal::getName, setmealDto.getName());
        queryWrapperSameNameDeleted.eq(Setmeal::getIsDeleted, 1);
        int count = setmealService.count(queryWrapperSameNameDeleted);

        if (count == 1) {
            //存在同名删除后的分类，删除已存在的数据，重新插入新的数据
            //因为name为索引字段，具有唯一性，用name来取id
            Setmeal setmealDeleted = setmealService.getOne(queryWrapperSameNameDeleted);
            //彻底移除
            setmealService.removeById(setmealDeleted.getId());
        }

        //清理干净后或者不存在旧记录则保存套餐基本信息
        boolean addSetmeal = this.save(setmealDto);

        //通过套餐id获取套餐菜品
        Long setmealDtoId = setmealDto.getId();
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();

        setmealDishList = setmealDishList.stream().peek((item) -> {
            item.setSetmealId(setmealDtoId);
            item.setId(IdWorker.getId());
        }).collect(Collectors.toList());

        //保存套餐菜品信息
        boolean addSetmealDish = setmealDishService.saveBatch(setmealDishList);

        //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        return addSetmeal && addSetmealDish;
    }

    /**
     * 修改套餐
     *
     * @param setmealDto
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public Boolean updateSetmealWithDish(SetmealDto setmealDto) {

        //log.info("dishDto: {}",setmealDto);

        //更新前判断名字是否和已删除的菜品名发生冲突
        LambdaQueryWrapper<Setmeal> queryWrapperSameNameDeleted = new LambdaQueryWrapper<>();
        queryWrapperSameNameDeleted.eq(Setmeal::getName, setmealDto.getName());
        queryWrapperSameNameDeleted.eq(Setmeal::getIsDeleted, 1);
        int count = setmealService.count(queryWrapperSameNameDeleted);
        //删除标志
        boolean updateSetmeal = true;
        if (count == 1) {
            //存在同名删除后的分类，删除已存在的数据，重新插入新的数据
            //因为name为索引字段，具有唯一性，用name来取id
            Setmeal setmealDeleted = setmealService.getOne(queryWrapperSameNameDeleted);
            //彻底移除
            setmealService.removeById(setmealDeleted.getId());

        }

        //从套餐封装类中分离出套餐包含的属性
        //Setmeal setmeal = new Setmeal();
        //BeanUtils.copyProperties(setmealDto, setmeal, "setmealDishes", "categoryName");

        //更新dish表的基本信息
        updateSetmeal = this.updateById(setmealDto);

        //清理当前菜品对应的口味数据--dish_flaovr表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());

        //彻底删除套餐对应菜品表
        //setmealDishService.remove(queryWrapper);

        // 清理口味标志
        boolean deleteDish = true;
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        for (SetmealDish setmealDish : setmealDishList) {

            LambdaUpdateWrapper<SetmealDish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SetmealDish::getId, setmealDish.getId());
            updateWrapper.set(SetmealDish::getIsDeleted, 1);

            deleteDish = setmealDishService.update(updateWrapper);

        }

        // 添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();

        dishes = dishes.stream().peek((item) -> {
            item.setSetmealId(setmealDto.getId());
            // 重新填补菜品id
            item.setId(IdWorker.getId());
        }).collect(Collectors.toList());

        boolean updateDishes = setmealDishService.saveBatch(dishes);

        //添加当前提交过来的口味数据--dish_flavor表的insert操作
        //List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        //setmealDishes = setmealDishes.stream().peek((item) -> {
        //    item.setSetmealId(setmealDto.getId());
        //}).collect(Collectors.toList());
        //setmealDishService.saveBatch(setmealDishes)

        return updateSetmeal && deleteDish && updateDishes;
    }

    /**
     * 删除套餐及关联菜品
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
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

        //删除菜品成功标志
        boolean deleteSetmeal = true;
        // 删除菜品，伪删除，更新其is_deleted字段为1，先更新dish表
        for (Long id : ids) {
            LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(null != id, Setmeal::getId, id);
            updateWrapper.set(Setmeal::getIsDeleted, 1);
            boolean update = setmealService.update(updateWrapper);
            //有其一更新失败则整体失败
            if (!update) {
                deleteSetmeal = false;
            }
        }

        //彻底删除，如果可以删除，先删除套餐表中的数据--setmeal
        //this.removeByIds(ids);

        //更新套餐对应菜品is_delete字段
        //删除口味表成功标志
        boolean deleteDishFlavor = true;
        // 遍历ids又同时遍历dishFlavor更新菜品
        for (Long id : ids) {
            LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SetmealDish::getSetmealId, id);
            List<SetmealDish> setmealDishList = setmealDishService.list(wrapper);

            for (SetmealDish setmealDish : setmealDishList) {
                LambdaUpdateWrapper<SetmealDish> updateWrapperSetmealDish = new LambdaUpdateWrapper<>();
                updateWrapperSetmealDish.eq(null != setmealDish.getId(), SetmealDish::getId, setmealDish.getId());
                updateWrapperSetmealDish.set(SetmealDish::getIsDeleted, 1);
                Boolean update = setmealDishService.update(updateWrapperSetmealDish);
                //有其一更新失败则整体失败
                if (!update) {
                    deleteDishFlavor = false;
                }
            }
        }

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        //LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        //删除关系表中的数据---setmeal dish
        //return setmealDishService.remove(lambdaQueryWrapper);

        return deleteSetmeal && deleteDishFlavor;
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

    /**
     * 根据id查询套餐信息和对应的菜品信息
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdSetmealWithDish(Long id) {

        //log.info("id: {}",id);

        LambdaQueryWrapper<Setmeal> queryWrapperDeletedById = new LambdaQueryWrapper<>();
        queryWrapperDeletedById.eq(Setmeal::getId, id);
        queryWrapperDeletedById.eq(Setmeal::getIsDeleted, 1);
        int count = this.count(queryWrapperDeletedById);
        //count!=0说明该id已被删除
        if (count==1) {
            throw new CustomException("该id对应套餐已被删除");
        }
        //从setmeal表中查询套餐信息
        Setmeal setmeal = this.getById(id);

        if (setmeal==null) {
            throw new CustomException("该id对应套餐不存在");
        }

        //返回前端需要的类
        SetmealDto setmealDto = new SetmealDto();
        //拷贝菜品的普通属性
        BeanUtils.copyProperties(setmeal, setmealDto);

        //查询当前菜品对应的口味信息，从dish_flavor表中查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        queryWrapper.eq(SetmealDish::getIsDeleted, 0);
        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);

        //加上口味属性赋值
        setmealDto.setSetmealDishes(dishes);

        return setmealDto;
    }
}

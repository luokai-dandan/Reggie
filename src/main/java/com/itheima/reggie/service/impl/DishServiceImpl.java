package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    //@Resource
    //private RedisTemplate<String, List<DishDto>> redisTemplate;

    /**
     * 根据条件查询对应的菜品数据及分类信息和移动端的口味数据
     *
     * @param dish
     * @return
     */
    @Override
    public List<DishDto> getList(Dish dish) {

        //动态构造key
        //String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus(); //dish_1397844263642378242_1

        //先从redis中获取缓存数据
        //dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，直接返回，无需查询数据库
        //if (dishDtoList != null) {
        //    //如果存在，直接返回，无需查询数据库
        //    return dishDtoList;
        //}

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1的菜品
        queryWrapper.eq(dish.getStatus() != null, Dish::getStatus, dish.getStatus());
        //添加条件，查询未删除的菜品0
        queryWrapper.eq(Dish::getIsDeleted, 0);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();

            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //扩展数据，移动客户端需要菜品列表的口味信息，追加返回
            //item代表菜品dish
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        //redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return dishDtoList;
    }

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<DishDto> getPage(int page, int pageSize, String name) {

        //log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //执行查询
        // 第一个参数为函数执行条件，只有name不为空才会执行下面代码，查询字段名为Dish::getName，参数为name的数据
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        //添加条件，查询未删除的菜品0
        queryWrapper.eq(Dish::getIsDeleted, 0);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //将dishDto除了categoryName属性外的属性通过item拷贝过来
            BeanUtils.copyProperties(item, dishDto);
            //获取item属性的categoryId
            Long categoryId = item.getCategoryId(); //分类id
            //根据id查询分类对象及对象中name属性
            Category category = categoryService.getById(categoryId);
            //查到了再做以下操作
            if (category != null) {
                String categoryName = category.getName();
                //将name赋值到dishDto对象中，dishDto其他属性通过上面的BeanUtils.copyProperties拷贝得到
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return dishDtoPage;
    }

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDto
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public Boolean addDishWithFlavor(DishDto dishDto) {

        //log.info("dishDto: {}",dishDto);

        //更新前判断名字是否和已删除的菜品名差生冲突
        LambdaQueryWrapper<Dish> queryWrapperSameNameDeleted = new LambdaQueryWrapper<>();
        queryWrapperSameNameDeleted.eq(Dish::getName, dishDto.getName());
        queryWrapperSameNameDeleted.eq(Dish::getIsDeleted, 1);
        int count = dishService.count(queryWrapperSameNameDeleted);

        if (count == 1) {
            //存在同名删除后的分类，删除已存在的数据，重新插入新的数据
            //因为name为索引字段，具有唯一性，用name来取id
            Dish dishDeleted = dishService.getOne(queryWrapperSameNameDeleted);
            //彻底移除
            dishService.removeById(dishDeleted.getId());
        }

        //清理干净后或者不存在旧记录则保存菜品的基本信息到菜品表dish
        boolean addDish = this.save(dishDto);

        //List<DishFlavor>中未封装dishId
        Long dishId = dishDto.getId();  //菜品id

        List<DishFlavor> flavors = dishDto.getFlavors();

        //对集合的每个元素做相同的处理
        flavors = flavors.stream().peek((item) -> {
            item.setDishId(dishId);
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        boolean addFlavor = dishFlavorService.saveBatch(flavors);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //精确清理某个分类下面的菜品缓存
        //String key = "dish_" + dishDto.getCategoryId() + "_1";
        //redisTemplate.delete(key);

        return addDish && addFlavor;
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public Boolean updateDishWithFlavor(DishDto dishDto) {

        //log.info("dishDto: {}",dishDto);

        //更新dish表的基本信息
        //boolean updateDish = this.updateById(dishDto);

        //更新前判断名字是否和已删除的菜品名发生冲突
        LambdaQueryWrapper<Dish> queryWrapperSameNameDeleted = new LambdaQueryWrapper<>();
        queryWrapperSameNameDeleted.eq(Dish::getName, dishDto.getName());
        queryWrapperSameNameDeleted.eq(Dish::getIsDeleted, 1);
        int count = dishService.count(queryWrapperSameNameDeleted);
        //删除标志
        boolean updateDish = true;
        if (count == 1) {
            //存在同名删除后的分类，删除已存在的数据，重新插入新的数据
            //因为name为索引字段，具有唯一性，用name来取id
            Dish dishDeleted = dishService.getOne(queryWrapperSameNameDeleted);
            //彻底移除
            dishService.removeById(dishDeleted.getId());

        }
        //清理干净后或者不存在旧记录则更新dish表的基本信息
        updateDish = this.updateById(dishDto);

        // 清理当前菜品对应的口味数据--dish_flaovr表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        // 彻底清理之前的口味
        //dishFlavorService.remove(queryWrapper);

        // 清理口味标志
        boolean deleteFlavor = true;
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);
        for (DishFlavor dishFlavor : dishFlavorList) {
            LambdaUpdateWrapper<DishFlavor> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(DishFlavor::getId, dishFlavor.getId());
            updateWrapper.set(DishFlavor::getIsDeleted, 1);

            deleteFlavor = dishFlavorService.update(updateWrapper);

        }

        // 添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().peek((item) -> {
            item.setDishId(dishDto.getId());
            // 重新填补口味id
            item.setId(IdWorker.getId());
        }).collect(Collectors.toList());

        boolean updateFlavor = dishFlavorService.saveBatch(flavors);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //精确清理某个分类下面的菜品缓存
        //String key = "dish_" + dishDto.getCategoryId() + "_1";
        //redisTemplate.delete(key);

        return updateDish && deleteFlavor && updateFlavor;
    }

    /**
     * 删除菜品及关联表
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public Boolean deleteDishWithFlavor(List<Long> ids) {

        //select count(*) from dish where id in (1,2,3) and status = 1
        //查询菜品状态，确认是否可以删除
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        //添加条件，查询未删除的菜品0
        queryWrapper.eq(Dish::getIsDeleted, 0);
        int count = this.count(queryWrapper);

        //如果不能删除，抛出一个业务异常
        if (count > 0) {
            throw new CustomException("菜品正在售卖中，不能删除");
        }

        //可以删除
        //查询ids的所有dish获得分类id以便redis删除菜品对应的分类缓存
        //LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        //List<Dish> dishList = dishService.list(wrapper);

        //删除菜品成功标志
        boolean deleteDish = true;
        // 删除菜品，伪删除，更新其is_deleted字段为1，先更新dish表
        for (Long id : ids) {
            LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(null != id, Dish::getId, id);
            updateWrapper.set(Dish::getIsDeleted, 1);
            boolean update = dishService.update(updateWrapper);
            //有其一更新失败则整体失败
            if (!update) {
                deleteDish = false;
            }
        }

        //彻底删除菜品
        //boolean deleteDish = this.removeByIds(ids);

        //彻底删除口味表
        //delete from dish_flavor where dish_id in (1,2,3)
        //LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //lambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        //删除关系表中的数据---dish_flavor
        //boolean deleteFlavor = dishFlavorService.remove(lambdaQueryWrapper);


        //删除口味表成功标志
        boolean deleteDishFlavor = true;
        // 遍历ids又同时遍历dishFlavor更新菜品
        for (Long id : ids) {
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, id);
            List<DishFlavor> dishFlavors = dishFlavorService.list(wrapper);

            for (DishFlavor dishFlavor : dishFlavors) {
                LambdaUpdateWrapper<DishFlavor> updateWrapperDishFlavor = new LambdaUpdateWrapper<>();
                updateWrapperDishFlavor.eq(null != dishFlavor.getId(), DishFlavor::getId, dishFlavor.getId());
                updateWrapperDishFlavor.set(DishFlavor::getIsDeleted, 1);
                Boolean update = dishFlavorService.update(updateWrapperDishFlavor);
                //有其一更新失败则整体失败
                if (!update) {
                    deleteDishFlavor = false;
                }
            }
        }

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //精确清理某个分类下面的菜品缓存
        //for (Dish dish : dishList) {
        //    String key = "dish_" + dish.getCategoryId() + "_1";
        //    redisTemplate.delete(key);
        //}
        return deleteDish && deleteDishFlavor;
    }

    /**
     * 修改菜品状态
     *
     * @param ids
     * @param status
     * @return
     */
    @Override
    public Boolean updateDishStatus(List<Long> ids, Integer status) {

        if (ids.size() == 0) {
            throw new CustomException("状态修改异常");
        }

        ArrayList<Dish> dishList = new ArrayList<>();

        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            dishList.add(dish);
        }
        return dishService.updateBatchById(dishList);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdDishWithFlavor(Long id) {

        log.info("id: {}", id);

        //从dish表中查询菜品基本信息
        LambdaQueryWrapper<Dish> queryWrapperDeletedById = new LambdaQueryWrapper<>();
        queryWrapperDeletedById.eq(Dish::getId, id);
        queryWrapperDeletedById.eq(Dish::getIsDeleted, 1);
        int count = this.count(queryWrapperDeletedById);
        //count!=0说明该id已被删除
        if (count==1) {
            throw new CustomException("该id对应菜品已被删除");
        }
        Dish dish = this.getById(id);

        if (dish==null) {
            throw new CustomException("该id对应菜品不存在");
        }

        //动态构造key
        //String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus(); //dish_1397844263642378242_1

        //先从redis中获取缓存数据
        //List<DishDto> dishDtoList = redisTemplate.opsForValue().get(key);
        //如果存在，直接返回，无需查询数据库
        //if (dishDtoList != null) {
        //    //如果存在，直接返回，无需查询数据库，要能查出来，只能是一条数据
        //    return dishDtoList.get(0);
        //}

        //返回前端需要的类
        DishDto dishDto = new DishDto();
        //拷贝菜品的普通属性
        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表中查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        queryWrapper.eq(DishFlavor::getIsDeleted, 0);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //加上口味属性赋值
        dishDto.setFlavors(flavors);

        return dishDto;
    }
}

package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    @Transactional
    public Boolean addDishWithFlavor(DishDto dishDto) {

        //log.info("dishDto: {}",dishDto);
        //保存菜品的基本信息到菜品表dish
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
    @Transactional
    public Boolean updateDishWithFlavor(DishDto dishDto) {

        //log.info("dishDto: {}",dishDto);

        //更新dish表的基本信息
        boolean updateDish = this.updateById(dishDto);

        //清理当前菜品对应的口味数据--dish_flaovr表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().peek((item) -> {
            item.setDishId(dishDto.getId());
        }).collect(Collectors.toList());

        boolean updateFlavor = dishFlavorService.saveBatch(flavors);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //精确清理某个分类下面的菜品缓存
        //String key = "dish_" + dishDto.getCategoryId() + "_1";
        //redisTemplate.delete(key);

        return updateDish && updateFlavor;
    }

    /**
     * 删除菜品及关联表
     *
     * @param ids
     */
    @Override
    @Transactional
    public Boolean deleteDishWithFlavor(List<Long> ids) {

        //select count(*) from dish where id in (1,2,3) and status = 1
        //查询菜品状态，确认是否可以删除
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        int count = this.count(queryWrapper);

        //如果不能删除，抛出一个业务异常
        if (count > 0) {
            throw new CustomException("菜品正在售卖中，不能删除");
        }

        //可以删除
        //查询ids的所有dish获得分类id以便redis删除菜品对应的分类缓存
        //LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        //List<Dish> dishList = dishService.list(wrapper);

        //如果可以删除，先删除套餐表中的数据--dish
        boolean deleteDish = this.removeByIds(ids);

        //delete from dish_flavor where dish_id in (1,2,3)
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        //删除关系表中的数据---dish_flavor
        boolean deleteFlavor = dishFlavorService.remove(lambdaQueryWrapper);


        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //精确清理某个分类下面的菜品缓存
        //for (Dish dish : dishList) {
        //    String key = "dish_" + dish.getCategoryId() + "_1";
        //    redisTemplate.delete(key);
        //}
        return deleteDish && deleteFlavor;
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
        Dish dish = this.getById(id);

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
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //加上口味属性赋值
        dishDto.setFlavors(flavors);

        return dishDto;
    }
}

package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.OrdersDetail;
import com.itheima.reggie.entity.QueryPageDate;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mongo.entity.Order;
import com.itheima.reggie.service.OrdersDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
@Api(tags = "订单相关接口")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    //注入mongodb
    @Autowired
    private com.itheima.reggie.mongo.service.OrderService orderService;

    @Autowired
    private OrdersDetailService ordersDetailService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 提交订单
     * @param order
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation(value = "订单提交接口")
    @ApiImplicitParam(name = "order", value = "订单实体")
    public R<String> submit(@RequestBody Orders order) {

        ordersService.submit(order);
//        log.info("订单数据：{}", order);

        //清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("shoppingCart_*");
        redisTemplate.delete(keys);

        return R.success("订单提交成功，待派送");
    }

    /**
     * 手机端查询订单明细（使用mysql）
     * @param page
     * @param pageSize
     * @return
     */
//    @GetMapping("/userPage")
//    public R<Page<Orders>> userPage(int page, int pageSize) {
//        log.info("page = {}, pageSize = {}", page, pageSize);
//
//        //构造分页构造器
//        Page<Orders> pageInfo = new Page<Orders>(page, pageSize);
//        //构造条件构造器
//        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
//        //按照id查询
//        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
//        //添加排序条件
//        queryWrapper.orderByDesc(Orders::getOrderTime);
//        //执行查询
//        ordersService.page(pageInfo, queryWrapper);
//
//        return R.success(pageInfo);
//    }

    /**
     * 客户端通过mongodb查看订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    @ApiOperation(value = "手机端订单分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true)
    })
    public R<Page<OrdersDto>> userPage(int page, int pageSize) {
//        log.info("page = {}, pageSize = {}", page, pageSize);

        QueryPageDate queryPageDate = new QueryPageDate();
        queryPageDate.setPageAndPageSize(page, pageSize);

        List<OrdersDto> orderDtoList = ordersService.getOrdersAndDetail(queryPageDate);

        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        ordersDtoPage.setRecords(orderDtoList);
        ordersDtoPage.setTotal(orderDtoList.size());
//        ordersDtoPage.setOptimizeCountSql(true);
//        ordersDtoPage.setSearchCount(true);
//        ordersDtoPage.setHitCount(false);


        return R.success(ordersDtoPage);
    }


    /**
     * 网页端根据条件查询订单信息（使用mysql）
     * @param queryPageDate
     * @return
     */
//    @GetMapping("/page")
//    public R<Page<Orders>> page(QueryPageDate queryPageDate) {
//
//        int page = queryPageDate.getPage();
//        int pageSize = queryPageDate.getPageSize();
//        String number = queryPageDate.getNumber();
//        LocalDateTime beginTime = null;
//        LocalDateTime endTime = null;
//
//        if (queryPageDate.getBeginTime()!=null && queryPageDate.getEndTime()!=null) {
//            beginTime = queryPageDate.getBeginTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//            endTime = queryPageDate.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//        }
//
//        log.info("page = {}, pageSize = {}", page, pageSize);
//        log.info("beginTime = {}, endTime = {}", beginTime, endTime);
//
//        //构造分页构造器
//        Page<Orders> pageInfo = new Page<Orders>(page, pageSize);
//        //构造条件构造器
//        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
//
//        queryWrapper.like(StringUtils.isNotEmpty(number), Orders::getNumber, number);
//
//        if (beginTime != null && endTime != null) {
//            queryWrapper.between(Orders::getOrderTime, beginTime, endTime);
//        }
//
//        //添加排序条件
//        queryWrapper.orderByAsc(Orders::getOrderTime);
//        //执行查询
//        ordersService.page(pageInfo, queryWrapper);
//
//        return R.success(pageInfo);
//    }

    /**
     * mongo分页查询订单信息（管理端）
     * @param queryPageDate
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "管理端订单分页查询接口")
    @ApiImplicitParam(name = "queryPageDate", value = "分页查询实体")
    public R<Page<Order>> page(QueryPageDate queryPageDate) {
        List<Order> pageList = orderService.getPageList(queryPageDate);

        Page<Order> orderPage = new Page<>();

        orderPage.setRecords(pageList);

        //设置orderPage传给前端的总条数为pageList.size()
        //补全参数
        orderPage.setTotal(pageList.size());
        orderPage.setSize(queryPageDate.getPageSize());
        orderPage.setCurrent(queryPageDate.getPage());
        orderPage.setOptimizeCountSql(true);
        orderPage.setSearchCount(true);
        orderPage.setHitCount(false);

        return R.success(orderPage);
    }


    /**
     * 修改订单状态
     * @param order
     * @return
     */
    @PutMapping
    @ApiOperation(value = "订单状态修改接口")
    @ApiImplicitParam(name = "order", value = "订单实体")
    public R<String> status(@RequestBody Orders order){

//        log.info("order: {}", order);
        //更新数据库（使用mysql）
        ordersService.updateById(order);

        //将封端好的orders复制到mongodb的orders中
        Order orderMongo = new Order();
        BeanUtils.copyProperties(order, orderMongo);

        //在mongodb中更新status
        orderService.updateOrderStatus(orderMongo);

        return R.success("状态修改成功");
    }

    @PostMapping("/again")
    public R<String> again(@RequestBody Map<String,String> map){
        String ids = map.get("id");

        long id = Long.parseLong(ids);

        LambdaQueryWrapper<OrdersDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrdersDetail::getOrderId,id);
        //获取该订单对应的所有的订单明细表
        List<OrdersDetail> orderDetailList = ordersDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空，这里的clean方法是视频中讲过的,建议抽取到service中,那么这里就可以直接调用了
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(lambdaQueryWrapper);
        //清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("shoppingCart_*");
        redisTemplate.delete(keys);

        //获取用户id
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();
            if (dishId != null) {
                //如果是菜品那就添加菜品的查询条件
                shoppingCart.setDishId(dishId);
            } else {
                //添加到购物车的是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //把携带数据的购物车批量插入购物车表  这个批量保存的方法要使用熟练！！！
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");

    }
}

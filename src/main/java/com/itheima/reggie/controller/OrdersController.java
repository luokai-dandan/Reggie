package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.QueryPageDate;
import com.itheima.reggie.mongo.entity.Order;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    //注入mongodb
    @Autowired
    private com.itheima.reggie.mongo.service.OrderService orderService;


    /**
     * 提交订单
     * @param order
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders order) {

        ordersService.submit(order);
        log.info("订单数据：{}", order);

        return R.success("订单提交成功，待派送");
    }

    /**
     * 手机端查询订单明细
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
    public R<Page<Order>> userPage(int page, int pageSize) {
        log.info("page = {}, pageSize = {}", page, pageSize);

        QueryPageDate queryPageDate = new QueryPageDate();
        queryPageDate.setPage(page);
        queryPageDate.setPageSize(pageSize);

        List<Order> pageList = orderService.getPageList(queryPageDate);

        Page<Order> orderPage = new Page<>();

        orderPage.setRecords(pageList);

        return R.success(orderPage);
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
    public R<Page<Order>> page(QueryPageDate queryPageDate) {
        List<Order> pageList = orderService.getPageList(queryPageDate);

        Page<Order> orderPage = new Page<>();

        orderPage.setRecords(pageList);

        return R.success(orderPage);
    }


    /**
     * 修改订单状态
     * @param order
     * @return
     */
    @PutMapping
    public R<String> status(@RequestBody Orders order){

        log.info("order: {}", order);
        //更新数据库（使用mysql）
        ordersService.updateById(order);

        //将封端好的orders复制到mongodb的orders中
        Order orderMongo = new Order();
        BeanUtils.copyProperties(order, orderMongo);

        //在mongodb中更新status
        orderService.updateOrderStatus(orderMongo);

        return R.success("状态修改成功");
    }
}

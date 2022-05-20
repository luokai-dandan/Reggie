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
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation(value = "订单提交接口")
    //@ApiImplicitParam(name = "orders", value = "订单实体")
    public R<String> submit(@RequestBody Orders orders) {

        ordersService.submitOrders(orders);
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
     * 手机端通过mongodb查看订单信息
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


        Page<OrdersDto> ordersDtoPage = ordersService.getUserPage(page, pageSize);

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
    //@ApiImplicitParam(name = "queryPageDate", value = "分页查询实体")
    public R<Page<Order>> page(QueryPageDate queryPageDate) {

        Page<Order> orderPage = ordersService.getPage(queryPageDate);
        return R.success(orderPage);
    }


    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    @ApiOperation(value = "订单状态修改接口")
    //@ApiImplicitParam(name = "orders", value = "订单实体")
    public R<String> status(@RequestBody Orders orders){

        ordersService.updateOrdersStatus(orders);
        return R.success("状态修改成功");
    }

    /**
     * 再来一单
     * @param map
     * @return
     */
    @PostMapping("/again")
    @ApiOperation(value = "再来一单接口")
    //@ApiImplicitParam(name = "map", value = "订单实体")
    public R<String> again(@RequestBody Map<String,String> map){

        ordersService.againOrders(map);
        return R.success("操作成功");
    }

}

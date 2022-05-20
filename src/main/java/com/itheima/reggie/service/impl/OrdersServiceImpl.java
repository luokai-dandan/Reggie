package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.mongo.entity.Order;
import com.itheima.reggie.mongo.service.OrderService;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrdersDetailService ordersDetailService;

    //kafka
    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

    //kafka消息主题
    private final static String TOPIC_NAME = "order-topic";

    /**
     * 用户下单
     * @param order
     */
    @Override
    @Transactional
    public void submit(Orders order) {

        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();

        //查询当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        //防止其他方式导致购物车空仍提交数据
        if (shoppingCarts==null || shoppingCarts.size()==0) {
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据和地址数据
        User user = userService.getById(currentId);
        Long addressBookId = order.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook==null) {
            throw new CustomException("地址信息有误，不能下单");
        }

        //生成订单号
        long orderId = IdWorker.getId();

        AtomicInteger amount = new AtomicInteger(0);

        List<OrdersDetail> ordersDetailList = shoppingCarts.stream().map((item) -> {
            OrdersDetail ordersDetail = new OrdersDetail();

            ordersDetail.setOrderId(orderId);
            ordersDetail.setNumber(item.getNumber());
            ordersDetail.setDishFlavor(item.getDishFlavor());
            ordersDetail.setDishId(item.getDishId());
            ordersDetail.setSetmealId(item.getSetmealId());
            ordersDetail.setName(item.getName());
            ordersDetail.setImage(item.getImage());
            ordersDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return ordersDetail;
        }).collect(Collectors.toList());

        //填充其他信息
        order.setId(orderId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setStatus(2); //待派送
        order.setAmount(new BigDecimal(amount.get()));//总金额
        order.setUserId(currentId);
        order.setNumber(String.valueOf(orderId));
        order.setUserName(user.getName());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        order.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //将封端好的orders复制到mongodb的orders中
        Order orderMongo = new Order();
        BeanUtils.copyProperties(order, orderMongo);
        //向orderMongo中插入订单详情
        orderMongo.setOrdersDetail(ordersDetailList);

        //kafka发送订单消息
        String orderKey = "order_"+orderMongo.getId().toString();
        kafkaTemplate.send(TOPIC_NAME,0, orderKey, orderMongo);

        //向订单表插入数据，一条数据
        this.save(order);

        //向订单明细表插入数据，多条数据
        ordersDetailService.saveBatch(ordersDetailList);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    /**
     * 手机端查看订单及订单详情
     * @param queryPageDate
     * @return
     */
    @Override
    public List<OrdersDto> getOrdersAndDetail(QueryPageDate queryPageDate) {

        List<Order> orderList = orderService.getPageList(queryPageDate);

        List<OrdersDto> orderDtoList = new ArrayList<>();

        for(Order order: orderList){
            LambdaQueryWrapper<OrdersDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrdersDetail::getOrderId, order.getId());
            List<OrdersDetail> ordersDetailList = ordersDetailService.list(queryWrapper);

            OrdersDto orderDto = new OrdersDto();
            BeanUtils.copyProperties(order, orderDto);
            //对orderDto进行OrdersDetails属性的赋值
            orderDto.setOrdersDetails(ordersDetailList);

            orderDtoList.add(orderDto);
        }

        return orderDtoList;
    }
}

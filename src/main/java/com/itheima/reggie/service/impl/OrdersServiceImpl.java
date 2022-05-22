package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrdersService ordersService;

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

    @Autowired
    private RedisTemplate<Object, Object> RedisTemplate;

    /**
     * 用户下单
     *
     * @param orders
     */
    @Override
    @Transactional
    public void submitOrders(Orders orders) {

        //log.info("订单数据：{}", order);

        //获取当前用户id
        //Long currentId = BaseContext.getCurrentId();
        Long currentId = (Long) RedisTemplate.opsForValue().get("user");

        //查询当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        //防止其他方式导致购物车空仍提交数据
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据和地址数据
        User user = userService.getById(currentId);
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null) {
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
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2); //待派送
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //将封端好的orders复制到mongodb的orders中
        Order orderMongo = new Order();
        BeanUtils.copyProperties(orders, orderMongo);
        //向orderMongo中插入订单详情
        orderMongo.setOrdersDetail(ordersDetailList);

        //kafka发送订单消息
        String orderKey = "order_" + orderMongo.getId().toString();
        kafkaTemplate.send(TOPIC_NAME, 0, orderKey, orderMongo);

        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        ordersDetailService.saveBatch(ordersDetailList);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    /**
     * 手机端通过mongodb查看订单信息
     *
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page<OrdersDto> getUserPage(int page, int pageSize) {

        //log.info("page = {}, pageSize = {}", page, pageSize);

        QueryPageDate queryPageDate = new QueryPageDate();
        queryPageDate.setPage(page);
        queryPageDate.setPageSize(pageSize);

        List<Order> orderList = orderService.getPageList(queryPageDate);

        List<OrdersDto> orderDtoList = new ArrayList<>();

        for (Order order : orderList) {
            LambdaQueryWrapper<OrdersDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrdersDetail::getOrderId, order.getId());
            List<OrdersDetail> ordersDetailList = ordersDetailService.list(queryWrapper);

            OrdersDto orderDto = new OrdersDto();
            BeanUtils.copyProperties(order, orderDto);
            //对orderDto进行OrdersDetails属性的赋值
            orderDto.setOrdersDetails(ordersDetailList);

            orderDtoList.add(orderDto);
        }

        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        ordersDtoPage.setRecords(orderDtoList);
        ordersDtoPage.setTotal(orderDtoList.size());
        ordersDtoPage.setOptimizeCountSql(true);
        ordersDtoPage.setSearchCount(true);
        ordersDtoPage.setHitCount(false);

        return ordersDtoPage;
    }

    /**
     * mongo分页查询订单信息（管理端）
     *
     * @param queryPageDate
     * @return
     */
    @Override
    public Page<Order> getPage(QueryPageDate queryPageDate) {
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

        return orderPage;
    }

    /**
     * 修改订单状态
     *
     * @param orders
     * @return
     */
    @Override
    public void updateOrdersStatus(Orders orders) {

        //log.info("order: {}", order);
        //更新数据库（使用mysql）
        ordersService.updateById(orders);

        //将封端好的orders复制到mongodb的orders中
        Order orderMongo = new Order();
        BeanUtils.copyProperties(orders, orderMongo);

        //在mongodb中更新status
        orderService.updateOrderStatus(orderMongo);
    }

    /**
     * 再来一单
     *
     * @param map
     */
    @Override
    public void againOrders(Map<String, String> map) {

        String ids = map.get("id");
        long id = Long.parseLong(ids);

        //Long userId = BaseContext.getCurrentId();
        Long userId = (Long) RedisTemplate.opsForValue().get("user");

        LambdaQueryWrapper<OrdersDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrdersDetail::getOrderId, id);
        //获取该订单对应的所有的订单明细表
        List<OrdersDetail> orderDetailList = ordersDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空，这里的clean方法是视频中讲过的,建议抽取到service中,那么这里就可以直接调用了
        shoppingCartService.cleanSC();

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

    }

}

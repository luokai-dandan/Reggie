package com.itheima.reggie.mongo.service;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.entity.QueryPageDate;
import com.itheima.reggie.mongo.dao.OrderRepository;
import com.itheima.reggie.mongo.entity.Order;
import com.mongodb.BasicDBObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.beans.Expression;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//评论的业务层
@Slf4j
@Service
public class OrderService {
    //注入dao
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存一个评论
     *
     * @param order
     */
    public void saveOrder(Order order) {
        //如果需要自定义主键，可以在这里指定主键；如果不指定主键，MongoDB会自动生成主键
        //设置一些默认初始值。。。
        //调用dao`
        orderRepository.save(order);
    }

    /**
     * 更新评论
     *
     * @param order
     */
    public void updateOrder(Order order) {
        //调用dao
        orderRepository.save(order);
    }

    /**
     * 根据id删除评论
     *
     * @param id
     */
    public void deleteOrderById(String id) {
        //调用dao
        orderRepository.deleteById(id);
    }

    /**
     * 查询所有评论
     *
     * @return
     */
    public List<Order> findOrderList() {
        //调用dao
        return orderRepository.findAll();
    }

    /**
     * 根据id查询评论
     *
     * @param id
     * @return
     */
    public Order findOrderById(Long id) {
        //调用dao
        return orderRepository.findById(id.toString()).get();
    }

    /**
     * 分页查询
     * @param queryPageDate
     * @return
     */
    public List<Order> getPageList(QueryPageDate queryPageDate) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        int page = queryPageDate.getPage();
        int pageSize = queryPageDate.getPageSize();
        String number = queryPageDate.getNumber();

        log.info("beginTime: {}", queryPageDate.getBeginTime());
        log.info("endTime: {}", queryPageDate.getEndTime());

        if (number != null) {
            criteria.and("number").is(number);
        }

        if (queryPageDate.getBeginTime()!=null && queryPageDate.getEndTime()!=null) {

            //Date
            Date beginTime = queryPageDate.getBeginTime();
            Date endTime = queryPageDate.getEndTime();

            criteria.andOperator(Criteria.where("orderTime").lte(endTime),
                                 Criteria.where("orderTime").gte(beginTime));

        }

        query.addCriteria(criteria);
        System.out.println(JSON.toJSONString(criteria));
        long count = mongoTemplate.count(query, Order.class); //计算总数,用于算法分页数
        System.out.println(count);

        int pageTotal = (int) (count % pageSize == 0 ? count / pageSize : count / pageSize + 1); //总页数
        System.out.println(pageTotal);
        int offset = (page - 1) * pageSize;
        query.with(Sort.by(Sort.Order.desc("orderTime"))); //排序逻辑
        query.skip(offset).limit(pageSize); // 分页逻辑

        //List<String> customerId = mongoTemplate.findDistinct(query, "customerId", ShopMongoOrder.class, String.class); //查询用户编号,去重
        return mongoTemplate.find(query, Order.class);
    }

    /**
     * 更新订单状态
     *
     * @param order
     */
    public void updateOrderStatus(Order order) {

        //查询条件
        Query query = Query.query(Criteria.where("_id").is(order.getId()));

        //更新条件
        Update update = new Update();
        update.set("status", order.getStatus());

        mongoTemplate.updateFirst(query, update, Order.class);
    }
}


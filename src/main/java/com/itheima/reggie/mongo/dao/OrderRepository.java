package com.itheima.reggie.mongo.dao;

import com.itheima.reggie.mongo.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

//mongodb的持久层接口
public interface OrderRepository extends MongoRepository<Order,String> {
}


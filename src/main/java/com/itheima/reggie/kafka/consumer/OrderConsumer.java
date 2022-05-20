package com.itheima.reggie.kafka.consumer;

import com.itheima.reggie.mongo.entity.Order;
import com.itheima.reggie.mongo.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderConsumer {

    //mongodb service
    @Autowired
    private OrderService orderService;

    private final static String TOPIC_NAME = "order-topic";


    @KafkaListener(topics = TOPIC_NAME, groupId = "MyGroup1")
    public void listenGroup(ConsumerRecord<String, Order> record, Acknowledgment ack) {

//    log.info("record : {}", record);

        Order order = record.value();
//    log.info("order: {}", order);

        orderService.saveOrder(order);
        //手动提交offset
        ack.acknowledge();
    }
}

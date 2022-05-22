package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 表字段自动填充
 */
@Slf4j
@Component
public class MyMetaObjecthandler implements MetaObjectHandler {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 插入操作自动填充
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {

        Long empolyeeId = (Long) redisTemplate.opsForValue().get("empolyee");

        log.info("公共字段自动填充[insert]...");

        //log.info(metaObject.toString());

        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
        //metaObject.setValue("createUser", empolyeeId);
        //metaObject.setValue("updateUser", empolyeeId);
    }

    /**
     * 更新操作自动填充
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {

        Long empolyeeId = (Long) redisTemplate.opsForValue().get("empolyee");

        log.info("公共字段自动填充[update]...");
        //log.info(metaObject.toString());

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
        //metaObject.setValue("updateUser", empolyeeId);
    }
}

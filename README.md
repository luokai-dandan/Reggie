# Reggie

#### 介绍
瑞吉外卖
网页端管理外卖后台和仿手机端模拟客户端订购外卖及提交外卖订单
哔哩哔哩 https://www.bilibili.com/video/BV13a411q753?spm_id_from=333.337.search-card.all.click

#### 软件架构
使用SpringBoot开发，前端使用Vue框架实现前后端分离，持久层使用MybatisPlus框架，
数据库使用MySQL5.7，订单信息及详情通过Kfaka消息队列发送到MongoDB中保存，
MySQL采用了主从复制存储数据，部分数据采用SpringData缓存到Redis中，
同时Redis也采用了主从复制，且MySQL和Redis从数据库都不允许增加(Create)、更新(Update)和删除(Delete)
只允许主数据库进行增加(Create)、更新(Update)和删除(Delete)，从数据库读取查询(Retrieve)

#### 安装教程
1. SpringBoot 2.4.5
2. MySQL 5.7
3. Redis 6.2.6
4. MongoDB 5.0.5
5. Kafka 2.3.1
6. jdk 8
7. Maven 3.6.3   
7. Centos Linux 7
8. 短信平台使用腾讯云短信服务

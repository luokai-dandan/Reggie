# Reggie

### 介绍
瑞吉外卖
网页端管理外卖后台和仿手机端模拟客户端订购外卖及提交外卖订单
哔哩哔哩 https://www.bilibili.com/video/BV13a411q753?spm_id_from=333.337.search-card.all.click

### 软件架构
1. 后台系统使用SpringBoot开发，前端使用Vue框架实现前后端分离，持久层使用MybatisPlus框架。
2. 数据库使用MySQL5.7，订单信息及详情通过Kfaka消息队列发送到MongoDB中保存，后台订单系统均通过MongoDB读取。
3. MySQL采用了主从复制存储数据，部分数据采用RedisTemplate和SpringCache缓存到Redis中。
4. 同时Redis也采用了主从复制，且MySQL和Redis从数据库都不允许增加(Create)、更新(Update)和删除(Delete)。
5. 只允许主数据库进行增加(Create)、更新(Update)和删除(Delete)，从数据库读取查询(Retrieve)。
6. 软件均通过Docker进行拉取安装，并通过docker安装Portainer并开放9000端口可视化docker容器和镜像。
7. 使用Swagger进行接口测试。

### 安装教程
1. SpringBoot 2.4.5
2. MySQL 5.7
3. Redis 6.2.6
4. MongoDB 5.0.5
5. Kafka 2.3.1
6. Jdk 8
7. Maven 3.6.3   
8. Centos Linux 7
9. 短信平台使用腾讯云短信服务
10. Docker 1.13.1

### 项目截图
#### docker安装所需软件
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/docker.png)
#### 登陆界面
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E7%99%BB%E9%99%86%E7%95%8C%E9%9D%A2.png)
#### 员工管理
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%91%98%E5%B7%A5%E7%AE%A1%E7%90%86.png)
#### 添加员工
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E6%B7%BB%E5%8A%A0%E5%91%98%E5%B7%A5.png)
#### 修改员工
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E4%BF%AE%E6%94%B9%E5%91%98%E5%B7%A5.png)
#### 菜品分类管理
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E8%8F%9C%E5%93%81%E5%88%86%E7%B1%BB%E7%AE%A1%E7%90%86.png)
#### 菜品管理
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E8%8F%9C%E5%93%81%E7%AE%A1%E7%90%86.png)
#### 添加菜品
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E6%B7%BB%E5%8A%A0%E8%8F%9C%E5%93%81.png)
#### 菜品分类预读取 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%88%86%E7%B1%BB%E9%A2%84%E8%AF%BB%E5%8F%96.png)
#### 菜品修改 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E8%8F%9C%E5%93%81%E4%BF%AE%E6%94%B9.png)
#### 套餐管理 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%A5%97%E9%A4%90%E7%AE%A1%E7%90%86.png)
#### 修改套餐 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E4%BF%AE%E6%94%B9%E5%A5%97%E9%A4%90.png)
#### 套餐分类预读取
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%A5%97%E9%A4%90%E5%88%86%E7%B1%BB%E9%A2%84%E8%AF%BB%E5%8F%96.png)
#### 套餐菜品预读取
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%A5%97%E9%A4%90%E8%8F%9C%E5%93%81%E9%A2%84%E8%AF%BB%E5%8F%96.png)
#### 添加套餐
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E6%B7%BB%E5%8A%A0%E5%A5%97%E9%A4%90.png)
#### 订单列表 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E8%AE%A2%E5%8D%95%E6%98%8E%E7%BB%86.png)
#### 查看订单
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E6%9F%A5%E7%9C%8B%E8%AE%A2%E5%8D%95.png)
#### 日期插件和关键字用来查询订单 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E6%97%A5%E6%9C%9F%E6%8F%92%E4%BB%B6.png)
#### 前台系统获取验证码自动注册登录 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E8%8E%B7%E5%8F%96%E9%AA%8C%E8%AF%81%E7%A0%81.png)
#### 前台首页 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%89%8D%E5%8F%B0%E9%A6%96%E9%A1%B5.png)
#### 选取菜品规格 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E9%80%89%E6%8B%A9%E8%8F%9C%E5%93%81%E8%A7%84%E6%A0%BC.png)
#### 前台菜品详情 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%89%8D%E5%8F%B0%E8%8F%9C%E5%93%81%E8%AF%A6%E6%83%85.png)
#### 前台套餐详情 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%89%8D%E5%8F%B0%E5%A5%97%E9%A4%90%E8%AF%A6%E6%83%8501.png)
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%89%8D%E5%8F%B0%E5%A5%97%E9%A4%90%E8%AF%A6%E6%83%8502.png)
#### 前台购物车 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E8%B4%AD%E7%89%A9%E8%BD%A6.png)
#### 前台提交订单界面 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E8%AE%A2%E5%8D%95%E7%95%8C%E9%9D%A2.png)
#### 前台地址管理 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E5%9C%B0%E5%9D%80%E7%AE%A1%E7%90%86.png)
#### 前台支付成功 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E6%94%AF%E4%BB%98%E6%88%90%E5%8A%9F.png)
#### 前台MongoDB存取订单
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/mongo%E8%AE%A2%E5%8D%95%E6%9F%A5%E8%AF%A2.png)
#### 前台kafka发送订单消息
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/kafka%E5%8F%91%E9%80%81%E6%B6%88%E6%81%AF.png)
#### 前台kafka接收消息
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/kafka%E6%8E%A5%E5%8F%97%E6%B6%88%E6%81%AF.png)
#### 前台查看订单和再来一单 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E8%AE%A2%E5%8D%95%E6%9F%A5%E8%AF%A2%2B%E5%86%8D%E6%9D%A5%E4%B8%80%E5%8D%95.png)
#### 前台个人主页 
![avatar](https://lk-1303842271.cos.ap-beijing.myqcloud.com/%E7%91%9E%E5%90%89%E5%A4%96%E5%8D%96%E9%A1%B9%E7%9B%AE%E6%88%AA%E5%9B%BE/%E4%B8%AA%E4%BA%BA%E4%B8%BB%E9%A1%B5.png)


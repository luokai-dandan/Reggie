package com.itheima.reggie.mongo.entity;

import com.itheima.reggie.entity.OrdersDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * mongodb订单实体类
 */
//把一个java类声明为mongodb的文档，可以通过collection参数指定这个类对应的文档。
//@Document(collection="mongodb 对应 collection 名")
// 若未加 @Document ，该 bean save 到 mongo 的 order collection
// 若添加 @Document ，则 save 到 order collection
@Document(collection = "order")//可以省略，如果省略，则默认使用类名小写映射集合
//复合索引
// @CompoundIndex( def = "{'userid': 1, 'nickname': -1}")
@Data
//@ApiModel("MongoDB订单实体")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    //主键标识，该属性的值会自动对应mongodb的主键字段"_id"，如果该属性名就叫“id”,则该注解可以省略，否则必须写
    @Id
    //@ApiModelProperty("主键")
    private Long id;

    //订单号
    //@ApiModelProperty("订单号")
    private String number;

    //订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
    //@ApiModelProperty("订单状态{1:待付款,2:待派送,3:已派送,4:已完成，,5:已取消}")
    private Integer status;

    //下单用户id
    @Indexed
    //@ApiModelProperty("下单用户编号")
    private Long userId;

    //地址id
    //@ApiModelProperty("地址编号")
    private Long addressBookId;

    //下单时间
    @ApiModelProperty("下单时间")
    private LocalDateTime orderTime;

    //结账时间
    //@ApiModelProperty("结账时间")
    private LocalDateTime checkoutTime;

    //支付方式 1微信，2支付宝
    //@ApiModelProperty("支付方式{1:微信,2:支付宝}")
    private Integer payMethod;

    //实收金额
    //@ApiModelProperty("订单金额")
    private BigDecimal amount;

    //备注
    //@ApiModelProperty("订单备注")
    private String remark;

    //用户名
    //@ApiModelProperty("下单用户名")
    private String userName;

    //手机号
    //@ApiModelProperty("下单用户手机号")
    private String phone;

    //地址
    //@ApiModelProperty("下单用户地址")
    private String address;

    //收货人
    //@ApiModelProperty("收货人")
    private String consignee;

    //订单详情
    //@ApiModelProperty("订单详情")
    private List<OrdersDetail> ordersDetail;
}

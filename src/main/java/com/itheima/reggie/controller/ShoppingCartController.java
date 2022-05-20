package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 购物车业务功能
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
@Api(tags = "购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 购物车列表
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "购物车列表接口")
    public R<List<ShoppingCart>> list(){

        List<ShoppingCart> shoppingCartList = shoppingCartService.getList();
        return R.success(shoppingCartList);
    }

    /**
     * 购物车加
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "+到购物车接口")
    //@ApiImplicitParam(name = "shoppingCart", value = "购物车实体")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {

        ShoppingCart sc = shoppingCartService.addDishToSC(shoppingCart);
        return R.success(sc);
    }

    /**
     * 购物车减
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation(value = "从购物车-接口")
    //@ApiImplicitParam(name = "shoppingCart", value = "购物车实体")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {

        ShoppingCart shoppingCartOne = shoppingCartService.subDishToSC(shoppingCart);
        return shoppingCartOne!=null?R.success(shoppingCartOne):R.error("操作失败");
    }

    /**
     * 根据用户id删除购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车接口")
    public R<String> clean(){

        shoppingCartService.cleanSC();
        return R.success("清空购物车成功");
    }
}

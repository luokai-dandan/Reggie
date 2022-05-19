package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
@Api(tags = "管理地址相关接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("/list")
    @ApiOperation(value = "地址列表查询接口")
    @ApiImplicitParam(name = "addressBook", value = "地址列表", required = true)
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        //当前无默认地址将第一条地址设为默认
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        int countAll = addressBookService.count(wrapper);
        wrapper.eq(AddressBook::getIsDefault, 1);
        int countDefault = addressBookService.count(wrapper);
        if (countAll>0 && countDefault==0){

            LambdaQueryWrapper<AddressBook> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
            lambdaQueryWrapper.eq(AddressBook::getIsDefault, 0);
            AddressBook one = addressBookService.getOne(lambdaQueryWrapper, false);
            one.setIsDefault(1);
            addressBookService.updateById(one);
        }

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //SQL:select * from address_book where user_id = ? order by update_time desc
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getIsDefault).orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> list = addressBookService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 新增保存地址信息
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增地址接口")
    @ApiImplicitParam(name = "addressBook", value = "地址实体")
    public R<AddressBook> add(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook: {}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 删除地址
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "删除地址接口")
    @ApiImplicitParam(name = "ids", value = "地址编号")
    public R<String> delete(long ids){
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")
    @ApiOperation(value = "设置默认地址接口")
    @ApiImplicitParam(name = "addressBook", value = "地址实体")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        wrapper.set(AddressBook::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);

        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "地址查询接口")
    @ApiImplicitParam(name = "id", value = "地址编号")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    @ApiOperation(value = "获取默认地址接口")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook) {
            return R.error("没有找到该对象");
        } else {
            return R.success(addressBook);
        }
    }

}

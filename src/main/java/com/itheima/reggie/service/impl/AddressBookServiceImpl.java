package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.mapper.AddressBookMapper;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 查询指定用户的全部地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> getList(AddressBook addressBook) {

        Long userId = (Long) redisTemplate.opsForValue().get("user");

        //addressBook.setUserId(BaseContext.getCurrentId());
        //log.info("addressBook:{}", addressBook);
        addressBook.setUserId(userId);

        //当前无默认地址将第一条地址设为默认
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        //查询该用户名下地址
        wrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, userId);
        wrapper.eq(AddressBook::getIsDeleted, 0);
        int countAll = addressBookService.count(wrapper);
        wrapper.eq(AddressBook::getIsDefault, 1);
        int countDefault = addressBookService.count(wrapper);
        if (countAll > 0 && countDefault == 0) {

            LambdaQueryWrapper<AddressBook> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, userId);
            lambdaQueryWrapper.eq( AddressBook::getIsDeleted, 0);
            lambdaQueryWrapper.eq(AddressBook::getIsDefault, 0);
            AddressBook one = addressBookService.getOne(lambdaQueryWrapper, false);
            one.setIsDefault(1);
            addressBookService.updateById(one);
        }

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //SQL:select * from address_book where user_id = ? order by update_time desc
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDeleted, 0);
        queryWrapper.orderByDesc(AddressBook::getIsDefault).orderByDesc(AddressBook::getUpdateTime);

        return addressBookService.list(queryWrapper);
    }

    /**
     * 新增保存地址信息
     *
     * @param addressBook
     * @return
     */
    @Override
    public AddressBook addAddr(AddressBook addressBook) {

        Long userId = (Long) redisTemplate.opsForValue().get("user");

        //addressBook.setUserId(BaseContext.getCurrentId());
        //设置添加地址所属用户为当前用户
        addressBook.setUserId(userId);
        //填写空白字段
        addressBook.setCreateTime(LocalDateTime.now());
        addressBook.setCreateUser(userId);
        addressBook.setUpdateTime(LocalDateTime.now());
        addressBook.setUpdateUser(userId);

        //查询是否当前用户下是否存在默认地址，不存在则设置当前添加的地址为默认地址
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, userId);
        queryWrapper.eq( AddressBook::getIsDeleted, 0);
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        int count = addressBookService.count(queryWrapper);
        if (count==0) {
            addressBook.setIsDefault(1);
        }

        //log.info("addressBook: {}", addressBook);
        addressBookService.save(addressBook);
        return addressBook;
    }

    /**
     * 删除地址
     *
     * @param ids
     * @return
     */
    @Override
    public Boolean deleteAddr(long ids) {

        //return addressBookService.removeById(ids);
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getId, ids);
        //伪删除，将is_delete字段改为1
        updateWrapper.set(AddressBook::getIsDeleted, 1);

        return addressBookService.update(updateWrapper);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public AddressBook setDefaultAddr(AddressBook addressBook) {

        Long userId = (Long) redisTemplate.opsForValue().get("user");

        //log.info("addressBook:{}", addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId);
        wrapper.eq(AddressBook::getIsDeleted, 0);
        wrapper.set(AddressBook::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);

        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);

        return addressBook;
    }

    /**
     * 根据id查询地址
     *
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(long id) {

        return addressBookService.getById(id);
    }

    /**
     * 查询默认地址
     *
     * @return
     */
    @Override
    public AddressBook getAddrDefault() {

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, redisTemplate.opsForValue().get("user"));
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        queryWrapper.eq(AddressBook::getIsDeleted, 0);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        return addressBookService.getOne(queryWrapper);
    }

    /**
     * 修改地址
     *
     * @return
     */
    @Override
    public Boolean updateAddr(AddressBook addressBook) {

        return addressBookService.updateById(addressBook);
    }

}

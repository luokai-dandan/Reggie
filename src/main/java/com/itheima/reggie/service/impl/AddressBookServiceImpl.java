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
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询指定用户的全部地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> getList(AddressBook addressBook) {

        addressBook.setUserId(BaseContext.getCurrentId());
        //log.info("addressBook:{}", addressBook);

        //当前无默认地址将第一条地址设为默认
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        int countAll = addressBookService.count(wrapper);
        wrapper.eq(AddressBook::getIsDefault, 1);
        int countDefault = addressBookService.count(wrapper);
        if (countAll > 0 && countDefault == 0) {

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

        List<AddressBook> addressBookList = addressBookService.list(queryWrapper);

        return addressBookList;
    }

    /**
     * 新增保存地址信息
     *
     * @param addressBook
     * @return
     */
    @Override
    public AddressBook addAddr(AddressBook addressBook) {

        addressBook.setUserId(BaseContext.getCurrentId());
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

        return addressBookService.removeById(ids);
    }

    /**
     * 设置总有一个默认地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public AddressBook setDefaultAddr(AddressBook addressBook) {

        //log.info("addressBook:{}", addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
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
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

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

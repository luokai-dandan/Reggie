package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.AddressBook;

import java.util.List;


public interface AddressBookService extends IService<AddressBook> {

    /**
     * 查询指定用户的全部地址
     *
     * @param addressBook
     * @return
     */
    public List<AddressBook> getList(AddressBook addressBook);

    /**
     * 新增保存地址信息
     *
     * @param addressBook
     * @return
     */
    public AddressBook addAddr(AddressBook addressBook);

    /**
     * 删除地址
     *
     * @param ids
     * @return
     */
    public Boolean deleteAddr(long ids);

    /**
     * 设置总有一个默认地址
     *
     * @param addressBook
     * @return
     */
    public AddressBook setDefaultAddr(AddressBook addressBook);

    /**
     * 根据id查询地址
     *
     * @param id
     * @return
     */
    public AddressBook getById(long id);

    /**
     * 查询默认地址
     *
     * @return
     */
    public AddressBook getAddrDefault();

    /**
     * 修改地址
     *
     * @return
     */
    public Boolean updateAddr(AddressBook addressBook);
}

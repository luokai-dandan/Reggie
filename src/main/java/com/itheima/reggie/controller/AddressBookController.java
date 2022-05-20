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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    //@ApiImplicitParam(name = "addressBook", value = "地址列表")
    @Cacheable(value = "addressBookCache", key = "#addressBook.id + '_' + #addressBook.userId")
    public R<List<AddressBook>> list(AddressBook addressBook) {

        List<AddressBook> addressBookList = addressBookService.getList(addressBook);
        return addressBookList != null ? R.success(addressBookList) : R.error("查询错误");
    }

    /**
     * 新增保存地址信息
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增地址接口")
    //@ApiImplicitParam(name = "addressBook", value = "地址实体")
    @CacheEvict(value = "addressBookCache", allEntries = true)
    public R<AddressBook> add(@RequestBody AddressBook addressBook) {

        AddressBook addAddr = addressBookService.addAddr(addressBook);
        return addAddr != null ? R.success(addAddr) : R.error("查询错误");
    }

    /**
     * 删除地址
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "删除地址接口")
    //@ApiImplicitParam(name = "ids", value = "地址编号")
    //删除setmealCache分类下的所有缓存数据
    @CacheEvict(value = "addressBookCache", allEntries = true)
    public R<String> delete(long ids) {

        Boolean remove = addressBookService.deleteAddr(ids);
        return remove ? R.success("删除成功") : R.error("删除失败");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")
    @ApiOperation(value = "设置默认地址接口")
    //@ApiImplicitParam(name = "addressBook", value = "地址实体")
    @CacheEvict(value = "addressBookCache", allEntries = true)
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {

        AddressBook setDefaultAddr = addressBookService.setDefaultAddr(addressBook);
        return setDefaultAddr != null ? R.success(setDefaultAddr) : R.error("查询错误");
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "地址查询接口")
    //@ApiImplicitParam(name = "id", value = "地址编号")
    @Cacheable(value = "addressBookCache", key = "#id", unless = "#result == null")
    public R<AddressBook> get(@PathVariable Long id) {

        AddressBook addressBook = addressBookService.getById(id);
        return addressBook != null ? R.success(addressBook) : R.error("查询错误");
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    @ApiOperation(value = "获取默认地址接口")
    public R<AddressBook> getDefault() {

        AddressBook addressBook = addressBookService.getAddrDefault();
        return addressBook != null ? R.success(addressBook) : R.error("查询错误");

    }

    /**
     * 修改地址
     */
    @PutMapping
    @ApiOperation(value = "修改地址")
    //@ApiImplicitParam(name = "addressBook", value = "地址实体")
    @CacheEvict(value = "addressBookCache", allEntries = true)
    public R<String> update(@RequestBody AddressBook addressBook) {

        Boolean update = addressBookService.updateAddr(addressBook);
        return update ? R.success("地址修改成功") : R.error("修改失败");
    }

}

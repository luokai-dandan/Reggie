package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/employee")
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录系统，返回对象包含失败标志和成功对象
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "管理端员工登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "请求对象", required = true),
            @ApiImplicitParam(name = "employee", value = "员工实体", required = true)
    })
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、查询用户
        Employee user = employeeService.queryUser(employee);

        //3、如果没有查询到则返回登录失败结果
        if (user == null) {
            return R.error("账号已过期，请重新登录");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if (!user.getPassword().equals(password)) {
            return R.error("密码错误，登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (user.getStatus() == 0) {
            return R.error("账号已过期，请重新登录");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", user.getId());

        return R.success(user);
    }

    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "管理端员工登出接口")
    //@ApiImplicitParam(name = "request", value = "请求对象")
    public R<String> logout(HttpServletRequest request) {

        Boolean logout = employeeService.logout(request);

        return logout ? R.success("退出成功") : R.error("退出失败");
    }

    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "员工分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true),
            @ApiImplicitParam(name = "name", value = "员工姓名", required = false)
    })
    public R<Page<Employee>> page(int page, int pageSize, String name) {

        Page<Employee> employeePage = employeeService.getPage(page, pageSize, name);
        return employeePage != null ? R.success(employeePage) : R.error("查询错误");
    }

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    @ApiOperation(value = "套餐分页查询接口")
    //@ApiImplicitParam(name = "employee", value = "员工实体")
    public R<String> save(@RequestBody Employee employee) {

        Boolean addEmployee = employeeService.addEmployee(employee);
        return addEmployee ? R.success("新增员工成功") : R.error("新增员工失败");
    }

    /**
     * 根据id修改员工信息
     *
     * @param employee
     * @return
     */
    @PutMapping
    @ApiOperation(value = "员工信息修改接口")
    //@ApiImplicitParam(name = "employee", value = "员工实体")
    public R<String> update(@RequestBody Employee employee) {

        Boolean update = employeeService.updateEmployee(employee);
        return update ? R.success("员工信息修改成功") : R.error("员工信息修改失败");
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "员工信息查询接口")
    //@ApiImplicitParam(name = "id", value = "员工编号")
    public R<Employee> getById(@PathVariable Long id) {

        Employee employee = employeeService.getEmployeeById(id);
        return employee != null ? R.success(employee) : R.error("查询错误");
    }
}

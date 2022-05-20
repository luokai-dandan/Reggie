package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录系统，返回登录对象
     *
     * @param employee
     * @return
     */
    @Override
    public Employee queryUser(Employee employee) {

        //根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());

        return employeeService.getOne(queryWrapper);
    }

    @Override
    public void login(Employee employee, HttpServletRequest request, HttpServletResponse response) {

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", employee.getId());

        //写入cookie
        Cookie cookieUserId = new Cookie("loginUser", employee.getId().toString());
        cookieUserId.setMaxAge(10 * 24 * 60 * 60);
        response.addCookie(cookieUserId);

        Cookie cookieCode = new Cookie("loginPwd", employee.getPassword());
        cookieCode.setMaxAge(10 * 24 * 60 * 60);
        response.addCookie(cookieCode);

    }

    @Override
    public Boolean logout(HttpServletRequest request, HttpServletResponse response) {

        //清理Session中保存的当前登陆员工的id
        request.getSession().removeAttribute("employee");

        //写入cookie
        Cookie cookieUserId = new Cookie("loginUser", "");
        cookieUserId.setMaxAge(0);
        response.addCookie(cookieUserId);

        Cookie cookieCode = new Cookie("loginPwd", "");
        cookieCode.setMaxAge(0);
        response.addCookie(cookieCode);

        return true;
    }

    @Override
    public Page<Employee> getPage(int page, int pageSize, String name) {

        //log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //执行查询
        // 第一个参数为函数执行条件，只有name不为空才会执行下面代码，查询字段名为Employee::getName，参数为name的数据
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //创建用户为1
        queryWrapper.eq(Employee::getCreateUser, BaseContext.getCurrentId());
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, queryWrapper);

        return pageInfo;
    }

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @Override
    public Boolean addEmployee(Employee employee) {
        //log.info("新增员工，员工信息：{}", employee.toString());

        // 设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        return employeeService.save(employee);
    }

    /**
     * 修改员工信息
     *
     * @param employee
     * @return
     */
    @Override
    public Boolean updateEmployee(Employee employee) {

        //log.info("employee:{}", employee.toString());
        return employeeService.updateById(employee);
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @Override
    public Employee getEmployeeById(Long id) {

        //log.info("根据id查询员工信息");
        Employee employee = employeeService.getById(id);
        return employee;
    }

}

package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Employee;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface EmployeeService extends IService<Employee> {

    /**
     * 登录系统，返回登录对象
     *
     * @param employee
     * @return
     */
    public Employee queryUser(Employee employee);

    /**
     * 登录系统
     *
     * @param employee
     * @param response
     * @return
     */
    public void login(Employee employee, HttpServletRequest request, HttpServletResponse response);

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    public Boolean logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    public Page<Employee> getPage(int page, int pageSize, String name);

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    public Boolean addEmployee(Employee employee);

    /**
     * 修改员工信息
     *
     * @param employee
     * @return
     */
    public Boolean updateEmployee(Employee employee);

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    public Employee getEmployeeById(Long id);

}
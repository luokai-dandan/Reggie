package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public interface UserService extends IService<User> {

    /**
     * 发送短信验证码
     * @param user
     * @param session
     * @return
     */
    public Integer sendMessage(User user, HttpSession session);

    /**
     * 登陆验证
     * @param map
     * @param session
     * @return
     */
    public User phoneLogin(Map<String, String> map, HttpSession session);

    /**
     * 退出登录
     * @param request
     */
    public void Logout(HttpServletRequest request);
}

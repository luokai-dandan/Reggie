package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送短信验证码
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    @ApiOperation(value = "手机端发送验证码接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "用户实体", required = true),
            @ApiImplicitParam(name = "session", value = "session对象", required = true)
    })
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {

        // 1发送成功，-1发送失败
        Integer flag = userService.sendMessage(user, session);

        return flag == 1 ? R.success("手机验证码发送成功") : R.error("短信发送失败");

    }


    @PostMapping("login")
    @ApiOperation(value = "手机端用户登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "map", value = "手机号和验证码map对象", required = true),
            @ApiImplicitParam(name = "session", value = "session对象", required = true)
    })
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {

        User user = userService.phoneLogin(map, session);
        return user != null ? R.success(user) : R.error("登陆失败");
    }

    @PostMapping("/loginout")
    @ApiOperation(value = "手机端用户登出接口")
    @ApiImplicitParam(name = "request", value = "请求对象")
    public R<String> logout(HttpServletRequest request) {
        userService.Logout(request);
        return R.success("退出成功");
    }
}
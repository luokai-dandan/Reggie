package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 腾讯云短信相关参数
    @Value("${sendMsg.secretId}")
    private String secretId;
    @Value("${sendMsg.secretKey}")
    private String secretKey;
    @Value("${sendMsg.connTimeout}")
    private String connTimeout;
    @Value("${sendMsg.sdkAppId}")
    private String sdkAppId;
    @Value("${sendMsg.signName}")
    private String signName;
    @Value("${sendMsg.templateId}")
    private String templateId;
    @Value("${sendMsg.msgHead}")
    private String msgHead;


    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){

        log.info("user: {}", user.toString());
        log.info("userPhoneNumber: {}", user.getPhone());
        // 获取手机号
        String phoneNumber = user.getPhone();
        if (StringUtils.isNotEmpty(phoneNumber)) {
            // 生成随机的验证码
            //String verificationCode = ValidateCodeUtils.generateValidateCode(4).toString();
            String verificationCode = "1234";

            // 调用腾讯云短信服务API完成发送短信
            String[] phoneNumberSet = {"+86" + phoneNumber};
            //验证码数组
            String[] captchaParameters = {msgHead, verificationCode, connTimeout};
            log.info("验证码：{}", verificationCode);

            //SMSUtils.sendMessage(secretId, secretKey, connTimeout, sdkAppId, signName, templateId, captchaParameters, phoneNumberSet);

            // 需要将生成的验证码保存到Session中
            session.setAttribute(phoneNumber, verificationCode);

            return R.success("手机验证码发送成功");
        }

        return R.error("短信发送失败");
    }


    @PostMapping("login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session){

        // 前端传来的手机号和验证码
        String phone = map.get("phone");
        String code = map.get("code");

        //从Session中获取保存的验证码
        String codeInSession = (String) session.getAttribute(phone);

        //进行验证码的比对
        if (codeInSession != null && code.equals(codeInSession)) {
            // 比对成功，允许登录
            //判断当前手机号是否为新用户，如果是新用户完成自动注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            if (user == null) {
                // 新用户，完成自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //将用户信息放入session
            session.setAttribute("user", user.getId());
            //登陆成功
            return R.success(user);
        }

        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登陆员工的id
        request.getSession().removeAttribute("user");

        return R.success("退出成功");
    }
}
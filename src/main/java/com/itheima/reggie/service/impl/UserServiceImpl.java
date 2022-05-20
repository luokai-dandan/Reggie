package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserService userService;

    //注入redis
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 腾讯云短信相关参数
    @Value("${custom-parameters.send-msg.secret-id}")
    private String secretId;
    @Value("${custom-parameters.send-msg.secret-key}")
    private String secretKey;
    @Value("${custom-parameters.send-msg.conn-timeout}")
    private String connTimeout;
    @Value("${custom-parameters.send-msg.sdk-app-id}")
    private String sdkAppId;
    @Value("${custom-parameters.send-msg.sign-name}")
    private String signName;
    @Value("${custom-parameters.send-msg.template-id}")
    private String templateId;
    @Value("${custom-parameters.send-msg.msg-head}")
    private String msgHead;
    @Value("${custom-parameters.send-msg.enable}")
    private String enable;

    /**
     * 发送短信验证码
     *
     * @param user
     * @param session
     * @return
     */
    @Override
    public Integer sendMessage(User user, HttpSession session) {

        //log.info("user: {}", user.toString());
        //log.info("userPhoneNumber: {}", user.getPhone());
        // 获取手机号
        String phoneNumber = user.getPhone();
        if (StringUtils.isNotEmpty(phoneNumber)) {

            //验证码
            String verificationCode = null;

            //启用腾讯短信验证码
            if (enable.equals("true")) {
                // 生成随机的验证码
                verificationCode = ValidateCodeUtils.generateValidateCode(4).toString();
                // 调用腾讯云短信服务API完成发送短信
                String[] phoneNumberSet = {"+86" + phoneNumber};
                //验证码数组
                String[] captchaParameters = {msgHead, verificationCode, connTimeout};

                SMSUtils.sendMessage(secretId, secretKey, connTimeout, sdkAppId, signName, templateId, captchaParameters, phoneNumberSet);

            } else {
                //关闭腾讯短信验证码
                verificationCode = "1234";
            }

            log.info("短信验证码：{}", verificationCode);

            // 需要将生成的验证码保存到Session中
            session.setAttribute(phoneNumber, verificationCode);

            //将生成的验证码缓存到redis缓存中，并且设置有效期为5分钟
            redisTemplate.opsForValue().set(phoneNumber, verificationCode, Long.parseLong(connTimeout), TimeUnit.MINUTES);

            return 1;
        }

        return -1;
    }

    @Override
    public User phoneLogin(Map<String, String> map, HttpSession session, HttpServletResponse response) {

        // 前端传来的手机号和验证码
        String phone = map.get("phone");
        String code = map.get("code");

        //从Session中获取保存的验证码
        //String codeInSession = (String) session.getAttribute(phone);

        //从redis中获取缓存的验证码
        String codeInSession = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对
        if (code.equals(codeInSession)) {
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

            //写入cookie
            Cookie cooPhone = new Cookie("loginPhone", phone);
            cooPhone.setMaxAge(10 * 24 * 60 * 60);
            response.addCookie(cooPhone);

            Cookie cooCode = new Cookie("loginCode", code);
            cooCode.setMaxAge(10 * 24 * 60 * 60);
            response.addCookie(cooCode);

            //登陆成功，删除redis中缓存的验证码
            redisTemplate.delete(phone);

            return user;
        }
        return null;
    }

    /**
     * 退出登录
     *
     * @param request
     */
    @Override
    public void Logout(HttpServletRequest request, HttpServletResponse response) {
        //清理Session中保存的当前登陆员工的id
        request.getSession().removeAttribute("user");

        //清理cookie
        Cookie cooPhone = new Cookie("loginPhone", "1");
        cooPhone.setMaxAge(0);
        response.addCookie(cooPhone);

        Cookie cooCode = new Cookie("loginCode", "1");
        cooCode.setMaxAge(0);
        response.addCookie(cooCode);
    }

}

package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.controller.from.LoginForm;
import com.example.emos.wx.controller.from.RegisterFrom;
import com.example.emos.wx.db.service.contollerService.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 2657944563
 */
@RequestMapping("/user")
@RestController
@Api("微信登录")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    RedisTemplate redisTemplate;
    @Value("${emos.jwt.cache-expire}")
    private int checkExpire;

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterFrom register) {
        Integer userId = userService.registerUser(register.getRegisterCode(), register.getCode(), register.getNickName(), register.getUrl());
        Set<String> strings = userService.searchUserPermissions(userId);
        String token = jwtUtil.createToken(userId);
        saveTokenT2Redis(token, userId);
        System.out.println("Register:" + register);
        return R.ok("用户注册成功").put("token", token).put("permission", strings);
    }

    @PostMapping("/login")
    @ApiOperation("登录用户")
    public R login(@Valid @RequestBody LoginForm loginForm) {
        System.out.println("临时授权码" + loginForm.getCode() + "尝试登陆");
        Integer userId = userService.login(loginForm.getCode());
        System.out.println(userId + " 登录了");
        String token = jwtUtil.createToken(userId);
        saveTokenT2Redis(token, userId);
        Set<String> strings = userService.searchUserPermissions(userId);
        return R.ok("登录成功").put("permission", strings).put("token", token);


    }

    private void saveTokenT2Redis(String token, Integer userId) {
        redisTemplate.opsForValue().set(token, userId + "", checkExpire, TimeUnit.DAYS);
    }
}

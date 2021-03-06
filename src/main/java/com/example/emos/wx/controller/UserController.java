package com.example.emos.wx.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.controller.from.LoginForm;
import com.example.emos.wx.controller.from.RegisterFrom;
import com.example.emos.wx.db.pojo.TbDept;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.db.service.TbDeptService;
import com.example.emos.wx.db.service.TbUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
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
    TbDeptService deptService;
    @Autowired
    TbUserService userService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    RedisTemplate redisTemplate;
    @Value("${emos.jwt.cache-expire}")
    private int checkExpire;

    /**
     * 注册用户的接口
     *
     * @param register 注册用户的表单信息
     * @return 注册用户的结果信息
     */
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

    /**
     * 登录用户的接口
     *
     * @param loginForm 登录用户的表单信息，其实就一个临时授权码
     * @return 返回登录信息，权限列表，Token
     */
    @PostMapping("/login")
    @ApiOperation("登录用户")
    public R login(@Valid @RequestBody LoginForm loginForm, @RequestHeader("token") String requestToken) {
        if (!StrUtil.isBlank(requestToken)) {
            R.ok("登录成功").put("loginStatus", "keep/alive");
        }
        System.out.println("临时授权码" + loginForm.getCode() + "尝试登陆");
        Integer userId = userService.login(loginForm.getCode());
        System.out.println(userId + " 登录了");
        String token = jwtUtil.createToken(userId);
        saveTokenT2Redis(token, userId);
        System.out.println(token);
        Set<String> strings = userService.searchUserPermissions(userId);
        return R.ok("登录成功").put("permission", strings).put("token", token);


    }

    /**
     * 添加用户的接口
     *
     * @param code         微信临时授权码
     * @param registerCode 注册授权码
     * @return 返回用户注册信息，Token
     */
    @PostMapping("/add")
    @ApiOperation("添加用户")
    @RequiresPermissions(value = {"ROOT", "USER:ADD"}, logical = Logical.OR)
    public R addUser(String code, String registerCode) {
        return R.ok("用户添加成功").put("token", "token");
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户信息")
    public R searchUserSummary(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = new HashMap();
        TbUser user_id = userService.getOne(new QueryWrapper<TbUser>().eq("id", userId));
        if (user_id.getStatus() != 1) {
            return R.ok("员工离职");
        }
        map.put("name", user_id.getName());
        map.put("deptName", deptService.getOne(new QueryWrapper<TbDept>().eq("id", user_id.getDeptId())).getDeptName());
        map.put("photo", user_id.getPhoto());
        return R.ok().put("result", map);
    }

    /**
     * 复用方法：将token存放至redis
     *
     * @param token  存放的token字符串
     * @param userId 存放的用户id
     */
    private void saveTokenT2Redis(String token, Integer userId) {

        redisTemplate.opsForValue().set(token, userId + "", checkExpire, TimeUnit.DAYS);
    }
}

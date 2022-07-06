package com.example.emos.wx.controller;

import cn.hutool.core.date.DateUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.JwtUtil;
import com.example.emos.wx.db.service.contollerService.CheckinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author 2657944563
 */
@RequestMapping("/checkin")
@RestController
@Api("签到模块Web接口")
@Slf4j
public class CheckinController {
    @Resource
    CheckinService checkinService;

    @Resource
    JwtUtil jwtUtil;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看是否可签到")
    @RequiresPermissions("user:update")
    public R checkin(@RequestHeader("token") String token) {
        String s = checkinService.validCanCheckIn(jwtUtil.getUserId(token), DateUtil.now());
        return R.ok(s);
    }
}


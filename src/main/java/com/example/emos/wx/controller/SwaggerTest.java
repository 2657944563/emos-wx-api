package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Api("SwaggerTest")
public class SwaggerTest {

    @GetMapping("sayHello")
    @ApiOperation("Swagger测试方法")
    public R swagger() {
        return R.ok().put("msg", "hello swagger");
    }
}

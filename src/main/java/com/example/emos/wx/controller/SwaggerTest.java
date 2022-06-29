package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.controller.from.SwaggerTestFrom;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Api("SwaggerTest")
public class SwaggerTest {

    @GetMapping("sayHello")
    @ApiOperation("Swagger测试方法")
    public R swagger() {
        return R.ok().put("msg", "hello swagger");
    }

    @PostMapping("sayHello")
    @ApiOperation("Swagger测试方法")
    public R swagger(@Valid @RequestBody SwaggerTestFrom from) {
        return R.ok().put("msg", "hello swagger").put("name", from.getName());
    }
}

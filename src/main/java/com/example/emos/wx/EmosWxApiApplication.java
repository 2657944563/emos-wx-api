package com.example.emos.wx;

import com.example.emos.wx.db.service.TbRoleService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication
@MapperScan("com.example.emos.wx.db.mapper")
public class EmosWxApiApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(EmosWxApiApplication.class, args);

    }

}

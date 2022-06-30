package com.example.emos.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ServletComponentScan // 开启扫描servlet组件注解
//@MapperScan("com.example.emos.wx.db.mapper")
public class EmosWxApiApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(EmosWxApiApplication.class, args);


    }

}

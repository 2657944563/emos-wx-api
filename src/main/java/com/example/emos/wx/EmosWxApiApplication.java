package com.example.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.pojo.SysConfig;
import com.example.emos.wx.db.service.TbUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;

@SpringBootApplication
@ServletComponentScan // 开启扫描servlet组件注解
@Slf4j
//@MapperScan("com.example.emos.wx.db.mapper")
public class EmosWxApiApplication {

    @Value("${emos.face.image-folder}")
    private String imageFolder;
    @Resource
    TbUserService userService;
    @Resource
    SystemConstants systemConstants;

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(EmosWxApiApplication.class, args);


    }

    @PostConstruct
    public void init() {
//        创建临时人脸图片文件夹
        new File(imageFolder).mkdirs();
        for (SysConfig sysConfig : userService.allCheckTime()) {
            String paramKey = sysConfig.getParamKey();
            String paramValue = sysConfig.getParamValue();
            paramKey = StrUtil.toCamelCase(paramKey);
            try {
                Field field = systemConstants.getClass().getDeclaredField(paramKey);
                field.setAccessible(true);
                field.set(systemConstants, paramValue);
            } catch (Exception e) {
                log.error("paramKey " + paramKey + "  paramValue " + paramValue);
                log.error("日程时间格式错误", e);
            }
        }

    }
}

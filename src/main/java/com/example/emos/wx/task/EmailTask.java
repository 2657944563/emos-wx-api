package com.example.emos.wx.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Scope("prototype")
public class EmailTask {
    //yml文件中配置好了meail相关的信息就可以用这个对象发送邮件了
    @Resource
    private JavaMailSender javaMailSender;
    @Value("${emos.email.system}")
    private String system;

    /**
     * 异步执行发送邮件的方法
     *
     * @param message 发送邮件的邮件体
     */
    @Async
    public void sendAsync(SimpleMailMessage message) {
//        设置接收人
        message.setFrom(system);
//        发送邮件
        javaMailSender.send(message);
    }
}

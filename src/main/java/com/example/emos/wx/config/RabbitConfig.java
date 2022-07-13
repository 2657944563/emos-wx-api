package com.example.emos.wx.config;


import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${emos.mq.user}")
    private String username;
    @Value("${emos.mq.passwd}")
    private String password;
    @Value("${emos.mq.host}")
    private String host;

    @Bean
    public ConnectionFactory getConnectionFactory() {
        ConnectionFactory con = new ConnectionFactory();
        con.setHost(host);
        con.setPort(5672);
        con.setUsername(username);
        con.setPassword(password);
        return con;
    }
}

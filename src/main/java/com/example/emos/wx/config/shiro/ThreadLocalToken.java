package com.example.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalToken {
    private ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public void setToken(String token) {
        threadLocal.set(token);
    }

    public String getToken() {
        String token = threadLocal.get();
        return token;
    }

    public void clear() {
        threadLocal.remove();
    }
}

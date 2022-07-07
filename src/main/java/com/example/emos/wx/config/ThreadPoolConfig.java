package com.example.emos.wx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 配置一个springboot管理的线程池
 */
@Configuration
public class ThreadPoolConfig {
    @Bean("AsyncTaskExecutor")
    public AsyncTaskExecutor executor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(8);
        threadPool.setMaxPoolSize(16);
        threadPool.setQueueCapacity(32);
        threadPool.setKeepAliveSeconds(60);
        threadPool.setThreadNamePrefix("task-");
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPool.initialize();
        return threadPool;
    }
}

package com.rpc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        // 你可以根据需要调节核心线程数等
        return Executors.newScheduledThreadPool(1);
    }
}

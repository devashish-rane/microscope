package com.example.target.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "appExecutor")
    public Executor appExecutor(TaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(3);
        exec.setMaxPoolSize(3);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("app-exec-");
        exec.setTaskDecorator(taskDecorator);
        exec.initialize();
        return exec;
    }
}


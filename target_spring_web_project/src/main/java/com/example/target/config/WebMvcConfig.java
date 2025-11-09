package com.example.target.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AsyncTaskExecutor mvcExecutor;

    public WebMvcConfig(@Qualifier("appExecutor") Executor appExecutor, TaskDecorator taskDecorator) {
        if (appExecutor instanceof AsyncTaskExecutor ate) {
            this.mvcExecutor = ate;
        } else {
            SimpleAsyncTaskExecutor sae = new SimpleAsyncTaskExecutor("mvc-");
            sae.setTaskDecorator(taskDecorator);
            this.mvcExecutor = sae;
        }
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcExecutor);
    }
}

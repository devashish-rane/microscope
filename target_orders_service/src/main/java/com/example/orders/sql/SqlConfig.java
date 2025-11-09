package com.example.orders.sql;

import com.debugflow.core.event.EventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SqlConfig {
    @Bean
    public SqlCaptureTemplate sqlCaptureTemplate(JdbcTemplate jdbc, EventBus bus,
                                                 @Value("${spring.application.name:orders-svc}") String serviceName) {
        return new SqlCaptureTemplate(jdbc, bus, serviceName);
    }
}


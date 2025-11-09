package com.example.orders.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DbContainerConfig {
    private static final Logger log = LoggerFactory.getLogger(DbContainerConfig.class);

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "orders", name = "useTestcontainers", havingValue = "true", matchIfMissing = true)
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("ordersdb")
                .withUsername("sa")
                .withPassword("sa");
        log.info("Starting Testcontainers Postgres for Orders service...");
        return pg;
    }

    @Bean
    @ConditionalOnProperty(prefix = "orders", name = "useTestcontainers", havingValue = "true", matchIfMissing = true)
    public DataSource dataSource(PostgreSQLContainer<?> pg) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(pg.getJdbcUrl());
        ds.setUsername(pg.getUsername());
        ds.setPassword(pg.getPassword());
        ds.setMaximumPoolSize(5);
        return ds;
    }

    @Bean
    @ConditionalOnProperty(prefix = "orders", name = "useTestcontainers", havingValue = "true", matchIfMissing = true)
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }
}


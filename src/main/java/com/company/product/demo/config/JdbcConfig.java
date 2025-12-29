package com.company.product.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JdbcConfig {
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(
            DataSource dataSource) { // 不用 JdbcTemplate，直接 NamedParameter，避免字符串拼接
        return new NamedParameterJdbcTemplate(dataSource);
    }
}

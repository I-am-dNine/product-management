package com.company.product.demo.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class HealthRepository { //连线验证

    private final NamedParameterJdbcTemplate jdbc;

    public HealthRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer ping() {
        return jdbc.queryForObject(
                "SELECT 1",
                new HashMap<>(),
                Integer.class
        );
    }
}

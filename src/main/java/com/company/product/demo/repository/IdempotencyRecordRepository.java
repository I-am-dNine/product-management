package com.company.product.demo.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class IdempotencyRecordRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public IdempotencyRecordRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> findResourceId(String key) {
        String sql = """
            SELECT resource_id
            FROM idempotency_record
            WHERE idempotency_key = :key
        """;

        List<Long> results = jdbc.query(
                sql,
                Map.of("key", key),
                (rs, i) -> rs.getLong("resource_id")
        );

        return results.stream().findFirst();
    }

    public void save(String key, Long resourceId) {
        String sql = """
            INSERT INTO idempotency_record (idempotency_key, resource_id)
            VALUES (:key, :resourceId)
        """;

        jdbc.update(sql, Map.of(
                "key", key,
                "resourceId", resourceId
        ));
    }
}


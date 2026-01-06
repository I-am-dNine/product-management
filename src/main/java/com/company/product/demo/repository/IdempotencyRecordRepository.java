package com.company.product.demo.repository;

import org.springframework.dao.DataIntegrityViolationException;
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

    public boolean existsByKey(String key) {
        return findResourceId(key).isPresent();
    }

    public void save(String key, String action, Long resourceId) {
        String sql = """
                    INSERT INTO idempotency_record (idempotency_key, action, resource_id)
                    VALUES (:key, :action, :resourceId)
                """;

        jdbc.update(sql, Map.of(
                "key", key,
                "action", action,
                "resourceId", resourceId
        ));

    }

    /**
     * 尝试插入幂等记录，如果已存在则返回 false（表示是重复请求）
     * 兼容 MySQL 和 H2：先尝试插入，如果因唯一约束冲突失败则返回 false
     */
    public boolean tryInsert(String key, String action, Long resourceId) {
        String sql = """
                    INSERT INTO idempotency_record (idempotency_key, action, resource_id)
                    VALUES (:key, :action, :resourceId)
                """;

        try {
            int updated = jdbc.update(sql, Map.of(
                    "key", key,
                    "action", action,
                    "resourceId", resourceId
            ));
            // 返回 true 表示插入成功（第一次请求）
            return updated > 0;
        } catch (DataIntegrityViolationException e) {
            // 唯一约束冲突，表示记录已存在（重复请求）
            return false;
        }
    }
}



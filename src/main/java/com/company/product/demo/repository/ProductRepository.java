package com.company.product.demo.repository;

import com.company.product.demo.model.Product;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ProductRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Product> ROW_MAPPER = (rs, rowNum) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return p;
    };

    public void create(Product product) {
        String sql = """
            INSERT INTO product (name, price, stock)
            VALUES (:name, :price, :stock)
        """;

        jdbc.update(sql, Map.of(
                "name", product.getName(),
                "price", product.getPrice(),
                "stock", product.getStock()
        ));
    }

    public Product findById(Long id) {
        String sql = "SELECT * FROM product WHERE id = :id";
        return jdbc.queryForObject(sql, Map.of("id", id), ROW_MAPPER);
    }

    public List<Product> findAll() {
        return jdbc.query("SELECT * FROM product", ROW_MAPPER);
    }

    public void update(Product product) {
        String sql = """
            UPDATE product
            SET name = :name,
                price = :price,
                stock = :stock
            WHERE id = :id
        """;

        jdbc.update(sql, Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "price", product.getPrice(),
                "stock", product.getStock()
        ));
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM product WHERE id = :id", Map.of("id", id));
    }
}


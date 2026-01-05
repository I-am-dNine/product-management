package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class InventoryIdempotencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository repo;

    @Test
    void decreaseStock_shouldBeIdempotent_whenSameIdempotencyKeyProvided() {
        // given
        Product product = new Product("Test Product", new BigDecimal("100"), 10);
        repo.create(product);

        Long productId = product.getId();

        String idempotencyKey = "idem-stock-001";

        // when
        productService.decreaseStock(productId, 1, idempotencyKey);
        productService.decreaseStock(productId, 1, idempotencyKey);

        // then
        Product result = repo.findById(productId);
        assertEquals(9, result.getStock());
    }
}


package com.company.product.demo.service;

import com.company.product.demo.dto.CreateProductRequest;
import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test-idempotency")
class ProductServiceIdempotencyTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createProduct_shouldBeIdempotent_whenSameIdempotencyKeyProvided() {
        // given
        String idempotencyKey = "idem-key-001";

        CreateProductRequest request =
                new CreateProductRequest("MacBook Pro", new BigDecimal("8999"), 10);

        // when
        Product first;
        Product second;

        // first request
        first = productService.createProduct(request, idempotencyKey);

        // 模拟 request retry（新 transaction）
        second = productService.createProduct(request, idempotencyKey);

        // then
        assertEquals(first.getId(), second.getId());

        List<Product> all = productRepository.findAll();
        assertEquals(1, all.size());
    }
}

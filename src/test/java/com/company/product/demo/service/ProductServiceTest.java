package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
import com.company.product.demo.repository.IdempotencyRecordRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @Mock
    private RedisTemplate<String, Product> redisTemplate;

    @InjectMocks
    private ProductService productService;

    // ---------- create ----------

    @Test
    void create_should_delegate_to_repository() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStock(10);

        productService.create(product);

        verify(productRepository).create(product);
        verifyNoMoreInteractions(productRepository);
    }

    // ---------- updateProduct ----------

    @Test
    void updateProduct_should_delegate_to_repository_and_invalidate_cache() {
        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setName("Updated Name");
        product.setPrice(BigDecimal.valueOf(200));
        product.setStock(5);

        productService.updateProduct(product);

        verify(productRepository).update(product);
        verify(redisTemplate).delete(anyString());
    }

    // ---------- delete ----------

    @Test
    void delete_should_delegate_to_repository_and_invalidate_cache() {
        Long productId = 1L;

        productService.delete(productId);

        verify(productRepository).delete(productId);
        verify(redisTemplate).delete(anyString());
    }

    // ---------- decreaseStockAndFail ----------

    @Test
    void decreaseStockAndFail_should_decrease_stock_and_throw_exception() {
        Long productId = 1L;

        assertThrows(RuntimeException.class,
                () -> productService.decreaseStockAndFail(productId));

        verify(productRepository).decreaseStock(productId, 1);
        verifyNoMoreInteractions(productRepository);
    }
}

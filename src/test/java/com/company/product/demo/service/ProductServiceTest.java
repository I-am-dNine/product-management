package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
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

    // ---------- update ----------

    @Test
    void update_should_set_id_and_delegate_to_repository() {
        Long productId = 1L;

        Product product = new Product();
        product.setName("Updated Name");
        product.setPrice(BigDecimal.valueOf(200));
        product.setStock(5);

        productService.update(productId, product);

        assertEquals(productId, product.getId());
        verify(productRepository).update(product);
        verifyNoMoreInteractions(productRepository);
    }

    // ---------- delete ----------

    @Test
    void delete_should_delegate_to_repository() {
        Long productId = 1L;

        productService.delete(productId);

        verify(productRepository).delete(productId);
        verifyNoMoreInteractions(productRepository);
    }

    // ---------- decreaseStockAndFail ----------

    @Test
    void decreaseStockAndFail_should_decrease_stock_and_throw_exception() {
        Long productId = 1L;

        assertThrows(RuntimeException.class,
                () -> productService.decreaseStockAndFail(productId)
        );

        verify(productRepository).decreaseStock(productId, 1);
        verifyNoMoreInteractions(productRepository);
    }
}

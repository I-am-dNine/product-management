package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Disabled("Merged into ProductServiceTransactionTest")
public class ProductServiceTransactionTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void decreaseStock_shouldRollback_whenExceptionThrown() {
        // given
        Product product = new Product();
        product.setName("Rollback Test Product");
        product.setPrice(new BigDecimal("100"));
        product.setStock(5);

        productRepository.create(product);
        Long productId = product.getId();

        // when
        assertThrows(RuntimeException.class, () ->
                productService.decreaseStockAndFail(productId)
        );

        // then
        Product result = productRepository.findById(productId);
        assertEquals(5, result.getStock());  // rollback 成功
    }
}

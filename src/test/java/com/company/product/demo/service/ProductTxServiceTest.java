package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ProductTxServiceTest {

    @Autowired
    private ProductTxService productTxService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void transactional_should_rollback_on_runtime_exception() {
        Product p = new Product();
        p.setName("Tx Test");
        p.setPrice(new BigDecimal("100"));
        p.setStock(1);

        assertThrows(RuntimeException.class, () -> {
            productTxService.createThenFail(p);
        });

        assertEquals(0, productRepository.findAll().size());
    }
}

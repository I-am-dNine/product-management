package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class) // 启用 Mockito（JUnit 5）
class ProductServiceTest {

    @Mock // 建立一个「假的 Repository」（不会碰 DB）
    ProductRepository productRepository;

    @InjectMocks // 把假的 repo 注入到真的 service 里
    ProductService productService;

    @Test // create
    void create_should_call_repository_create() {
        // arrange（准备资料）
        Product product = new Product();
        product.setName("Test Product");
        product.setStock(10);

        // act（执行行为）
        productService.create(product);

        // assert（验证行为）
        verify(productRepository, times(1)).create(product);
    }

    @Test // update
    void update_should_set_id_and_call_repository_update() {
        // arrange
        Long id = 1L;
        Product product = new Product();
        product.setName("Updated Product");
        // act
        productService.update(id, product);
        // assert
        assertEquals(id, product.getId());
        verify(productRepository).update(product);
    }

    @Test
    void delete_should_call_repository_delete() {
        // arrange
        Long id = 1L;
        // act
        productService.delete(id);
        // assert
        verify(productRepository, times(1)).delete(id);
    }

}

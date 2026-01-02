package com.company.product.demo.repository;

import com.company.product.demo.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 让每个 Repository Test「彼此独立」
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @Test
    void create_and_findById_should_work() {
        // arrange
        Product p = new Product();
        p.setName("Test Product");
        p.setPrice(new BigDecimal("100.00"));
        p.setStock(5); // JDBC parameter binding ✔

        // act
        productRepository.create(p); // INSERT INTO product
        Product result = productRepository.findAll().get(0); //  RowMapper<Product> ✔ = SELECT * FROM product ✔

        // assert - schema 是否正确 ✔
        assertEquals("Test Product", result.getName());
        assertEquals(5, result.getStock());
    }

    @Test // 测 update 行为是否真的更新 DB
    void update_should_modify_existing_product() {
        Product p = new Product();
        p.setName("Original");
        p.setPrice(new BigDecimal("100.00"));
        p.setStock(5);

        productRepository.create(p);
        Product saved = productRepository.findAll().get(0);

        saved.setName("Updated");
        saved.setStock(10);
        productRepository.update(saved);

        Product updated = productRepository.findById(saved.getId());

        assertEquals("Updated", updated.getName());
        assertEquals(10, updated.getStock());
    }

    @Test // 测 DB 里有几笔，就拿回几笔
    void findAll_should_return_all_products() {
        // arrange
        Product p1 = new Product();
        p1.setName("Product A");
        p1.setPrice(new BigDecimal("100.00"));
        p1.setStock(5);

        Product p2 = new Product();
        p2.setName("Product B");
        p2.setPrice(new BigDecimal("200.00"));
        p2.setStock(10);

        productRepository.create(p1);
        productRepository.create(p2);

        // act
        List<Product> result = productRepository.findAll();

        // assert
        assertEquals(2, result.size());
    }

    @Test //  测 删完之后，DB 真的少一笔
    void delete_should_remove_product_from_db() {
        // arrange
        Product p = new Product();
        p.setName("To Be Deleted");
        p.setPrice(new BigDecimal("150.00"));
        p.setStock(3);

        productRepository.create(p);
        Product saved = productRepository.findAll().get(0);

        // act
        productRepository.delete(saved.getId());
        List<Product> result = productRepository.findAll();

        // assert
        assertEquals(0, result.size());
    }

    @Test // 并发测试
    void decreaseStock_concurrently_should_not_oversell() throws Exception {
        Product p = new Product();
        p.setName("Concurrent Test");
        p.setPrice(new BigDecimal("100"));
        p.setStock(1);

        productRepository.create(p);
        Long productId = p.getId();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        Runnable task = () -> {
            try {
                productRepository.decreaseStock(productId, 1);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        executor.submit(task);
        executor.submit(task);

        latch.await();

        Product result = productRepository.findById(productId);

        assertEquals(1, successCount.get());
        assertEquals(1, failCount.get());
        assertEquals(0, result.getStock());
    }


}



package com.company.product.demo.service;

import com.company.product.demo.dto.CreateProductRequest;
import com.company.product.demo.model.Product;
import com.company.product.demo.repository.IdempotencyRecordRepository;
import com.company.product.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repo;
    private final IdempotencyRecordRepository idempotencyRepo;


    public ProductService(ProductRepository repo, IdempotencyRecordRepository idempotencyRepo) {
        this.repo = repo;
        this.idempotencyRepo = idempotencyRepo;
    }

    public void create(Product p) {
        repo.create(p);
    }

    public Product get(Long id) {
        return repo.findById(id);
    }

    public List<Product> list() {
        return repo.findAll();
    }

    public void update(Long id, Product p) {
        p.setId(id);
        repo.update(p);
    }

    public void delete(Long id) {
        repo.delete(id);
    }

    public void decreaseStockAndFail(Long productId) {
        repo.decreaseStock(productId, 1);
        throw new RuntimeException("boom");
    }

    public Product createProduct(CreateProductRequest request, String idempotencyKey) {
        // 幂等逻辑 + transaction
        // 1. 先查幂等记录
        Optional<Long> existingId =
                idempotencyRepo.findResourceId(idempotencyKey);

        if (existingId.isPresent()) {
            // 已处理过，直接回传既有结果（不再写 DB）
            return repo.findById(existingId.get());
        }

        // 2. 第一次请求：真的创建
        Product product = new Product(
                request.getName(),
                request.getPrice(),
                request.getStock()
        );

        repo.create(product);

        // 3. 记录幂等 key → product.id
        idempotencyRepo.save(
                idempotencyKey,
                product.getId()
        );

        return product;
    }

    @Transactional
    public void decreaseStock(Long productId, int qty, String idemKey) {

        if (idempotencyRepo.existsByKey(idemKey)) {
            return;
        }

        repo.decreaseStock(productId, qty);

        idempotencyRepo.save(idemKey, productId);
    }

}


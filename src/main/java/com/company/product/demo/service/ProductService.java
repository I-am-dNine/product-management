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

    public static final String ACTION_CREATE_PRODUCT = "CREATE_PRODUCT";
    public static final String ACTION_DECREASE_PRODUCT = "DECREASE_PRODUCT";


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
                ACTION_CREATE_PRODUCT,
                product.getId()
        );

        return product;
    }

    public void decreaseStock(Long productId, int qty, String idemKey) {
        // 使用 "insert-first" 模式确保并发安全
        // 先尝试插入幂等记录，如果已存在（返回 false）说明是重复请求，直接返回
        boolean isFirstRequest = idempotencyRepo.tryInsert(idemKey, ACTION_DECREASE_PRODUCT, productId);
        
        if (!isFirstRequest) {
            // 重复请求，直接返回（幂等性保证）
            return;
        }

        // 第一次请求，执行扣库存
        // 如果扣库存失败，幂等记录已存在，后续相同请求会被拒绝（符合幂等性）
        repo.decreaseStock(productId, qty);
    }

}


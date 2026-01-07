package com.company.product.demo.service;

import com.company.product.demo.dto.CreateProductRequest;
import com.company.product.demo.model.Product;
import com.company.product.demo.repository.IdempotencyRecordRepository;
import com.company.product.demo.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String CACHE_KEY_PREFIX = "product:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private final ProductRepository repo;
    private final IdempotencyRecordRepository idempotencyRepo;
    private final RedisTemplate<String, Product> redis;

    public static final String ACTION_CREATE_PRODUCT = "CREATE_PRODUCT";
    public static final String ACTION_DECREASE_PRODUCT = "DECREASE_PRODUCT";

    public ProductService(ProductRepository repo, IdempotencyRecordRepository idempotencyRepo,
            RedisTemplate<String, Product> redis) {
        this.repo = repo;
        this.idempotencyRepo = idempotencyRepo;
        this.redis = redis;
    }

    public void create(Product p) {
        repo.create(p);
    }

    public List<Product> list() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.delete(id);
        redis.delete(CACHE_KEY_PREFIX + id);
    }

    public void decreaseStockAndFail(Long productId) {
        repo.decreaseStock(productId, 1);
        throw new RuntimeException("boom");
    }

    public Product createProduct(CreateProductRequest request, String idempotencyKey) {
        // 幂等逻辑 + transaction
        // 1. 先查幂等记录
        Optional<Long> existingId = idempotencyRepo.findResourceId(idempotencyKey);

        if (existingId.isPresent()) {
            // 已处理过，直接回传既有结果（不再写 DB）
            return repo.findById(existingId.get());
        }

        // 2. 第一次请求：真的创建
        Product product = new Product(
                request.getName(),
                request.getPrice(),
                request.getStock());

        repo.create(product);

        // 3. 记录幂等 key → product.id
        idempotencyRepo.save(
                idempotencyKey,
                ACTION_CREATE_PRODUCT,
                product.getId());

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

        // 关键：库存变更后，必须清除缓存，防止读到旧数据
        redis.delete(CACHE_KEY_PREFIX + productId);
    }

    /**
     * Read-through cache with Double-Checked Locking
     */
    public Product getProduct(Long productId) {
        String key = CACHE_KEY_PREFIX + productId;

        // 1. First check
        Product cached = redis.opsForValue().get(key);
        if (cached != null) {
            log.info("CACHE_HIT productId={}", productId);
            return cached;
        }

        // 2. Cache miss → Synchronized block (DCL)
        synchronized (this) {
            // 3. Second check (Double-Check)
            cached = redis.opsForValue().get(key);
            if (cached != null) {
                log.info("CACHE_HIT (DCL) productId={}", productId);
                return cached;
            }

            log.info("CACHE_MISS productId={}", productId);
            Product product = repo.findById(productId);

            if (product != null) {
                // 4. Fill cache inside lock
                redis.opsForValue().set(key, product, TTL);
            }

            return product;
        }
    }

    /**
     * Write path → invalidate cache
     */
    public void updateProduct(Product product) {
        repo.update(product);

        // 关键：写入后清 cache
        redis.delete(CACHE_KEY_PREFIX + product.getId());
    }

}

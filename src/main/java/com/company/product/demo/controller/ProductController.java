package com.company.product.demo.controller;

import com.company.product.demo.dto.CreateProductRequest;
import com.company.product.demo.model.Product;
import com.company.product.demo.service.ProductService;
import org.springframework.web.bind.annotation.*;
import com.company.product.demo.dto.DecreaseStockRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public void create(@RequestBody Product product) {
        service.create(product);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Product> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody Product product) {
        service.update(id, product);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/idemKey") // 幂等性｜产品建立
    public Product create(@RequestParam String idempotencyKey, @RequestBody CreateProductRequest product) {
        return service.createProduct(product, idempotencyKey);
    }

    @PostMapping("/{id}/decrease-stock") // 幂等性 + 并发安全｜库存扣减
    public ResponseEntity<Void> decreaseStock(
            @PathVariable("id") Long productId,
            @RequestBody DecreaseStockRequest request,
            @RequestParam String idempotencyKey
    ) {
        service.decreaseStock(productId, request.getQuantity(), idempotencyKey);
        return ResponseEntity.ok().build();
    }

}

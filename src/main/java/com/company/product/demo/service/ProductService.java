package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
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
}


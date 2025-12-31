package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import com.company.product.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void create(Product p) {
        repo.create(p);
        // 先不 throw，下一步再测
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
}


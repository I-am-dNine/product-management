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
    private final ProductTxService productTxService;

    public ProductService(ProductRepository repo, ProductTxService productTxService) {
        this.repo = repo;
        this.productTxService = productTxService;
    }

    public void create(Product product) {
        productTxService.createThenFail(product); // 外部调用
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


package com.company.product.demo.service;

import com.company.product.demo.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class ProductTxService {

    private ProductService productRepository;

    @Transactional
    public void createThenFail(Product product) {
        System.out.println(
                "TX active = " + TransactionSynchronizationManager.isActualTransactionActive()
        );

        productRepository.create(product);
        throw new RuntimeException("force failure");
    }
}



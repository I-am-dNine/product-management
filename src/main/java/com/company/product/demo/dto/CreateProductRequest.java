package com.company.product.demo.dto;

import java.math.BigDecimal;

public class CreateProductRequest {
    private final String name;
    private final BigDecimal price;
    private final int stock;

    public CreateProductRequest(String name, BigDecimal price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
}

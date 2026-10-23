package com.example.springstudy.dto;

import com.example.springstudy.domain.Product;

public record ProductResponse(Long id, String name, int price) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice());
    }
}

package com.example.springstudy.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.springstudy.domain.Product;
import com.example.springstudy.dto.ProductRequest;
import com.example.springstudy.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다. id=" + id));
    }

    public Product create(ProductRequest request) {
        validate(request);

        Product product = new Product(null, request.name(), request.price());
        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        findById(id);
        validate(request);

        Product product = new Product(id, request.name(), request.price());
        productRepository.update(id, product);
        return product;
    }

    public void delete(Long id) {
        findById(id);
        productRepository.deleteById(id);
    }

    private void validate(ProductRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상품명은 비어 있을 수 없습니다.");
        }

        if (request.price() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "가격은 0보다 커야 합니다.");
        }
    }
}

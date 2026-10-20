package com.example.springstudy.repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.example.springstudy.domain.Product;

@Repository
public class ProductRepository {

    private final ConcurrentHashMap<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<Product> findAll() {
        return List.copyOf(products.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public Product save(Product product) {
        Long id = idGenerator.incrementAndGet();
        Product savedProduct = new Product(id, product.getName(), product.getPrice());
        products.put(id, savedProduct);
        return savedProduct;
    }

    public void update(Long id, Product product) {
        Product updatedProduct = new Product(id, product.getName(), product.getPrice());
        products.put(id, updatedProduct);
    }

    public void deleteById(Long id) {
        products.remove(id);
    }
}

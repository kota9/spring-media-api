package com.example.springstudy.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.example.springstudy.domain.Category;

@Repository
public class CategoryRepository {
    private final ConcurrentHashMap<Long, Category> categories = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public List<Category> findAll() {
        return categories.values().stream().sorted(Comparator.comparing(Category::id)).toList();
    }

    public Optional<Category> findById(Long categoryId) {
        return Optional.ofNullable(categories.get(categoryId));
    }

    public Category save(String name, String description) {
        Category category = new Category(idGenerator.incrementAndGet(), name, description);
        categories.put(category.id(), category);
        return category;
    }

    public Category update(Long categoryId, String name, String description) {
        Category category = new Category(categoryId, name, description);
        categories.put(categoryId, category);
        return category;
    }

    public void deleteById(Long categoryId) {
        categories.remove(categoryId);
    }
}

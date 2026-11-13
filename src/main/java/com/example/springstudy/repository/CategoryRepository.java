package com.example.springstudy.repository;

import com.example.springstudy.domain.Category;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class CategoryRepository {
    private final JpaCategoryRepository categories;

    public CategoryRepository(JpaCategoryRepository categories) {
        this.categories = categories;
    }

    public List<Category> findAll() {
        return categories.findAll().stream().map(CategoryRepository::toDomain).toList();
    }

    public Optional<Category> findById(Long id) {
        return categories.findById(id).map(CategoryRepository::toDomain);
    }

    @Transactional
    public Category save(String name, String description) {
        return toDomain(categories.save(new com.example.springstudy.entity.Category(name, description)));
    }

    @Transactional
    public Category update(Long id, String name, String description) {
        com.example.springstudy.entity.Category category = categories.findById(id).orElseThrow();
        category.changeName(name);
        category.changeDescription(description);
        return toDomain(category);
    }

    @Transactional
    public void deleteById(Long id) {
        categories.deleteById(id);
    }

    private static Category toDomain(com.example.springstudy.entity.Category category) {
        return new Category(category.getId(), category.getName(), category.getDescription());
    }
}

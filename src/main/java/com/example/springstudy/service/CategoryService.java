package com.example.springstudy.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springstudy.domain.Category;
import com.example.springstudy.dto.CategoryRequest;
import com.example.springstudy.exception.CategoryNotFoundException;
import com.example.springstudy.repository.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    public Category create(CategoryRequest request) {
        return categoryRepository.save(request.name(), request.description());
    }

    public Category update(Long categoryId, CategoryRequest request) {
        findById(categoryId);
        return categoryRepository.update(categoryId, request.name(), request.description());
    }

    public void delete(Long categoryId) {
        findById(categoryId);
        categoryRepository.deleteById(categoryId);
    }
}

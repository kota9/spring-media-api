package com.example.springstudy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    @Transactional
    public Category create(CategoryRequest request) {
        return categoryRepository.save(request.name(), request.description());
    }

    @Transactional
    public Category update(Long categoryId, CategoryRequest request) {
        findById(categoryId);
        return categoryRepository.update(categoryId, request.name(), request.description());
    }

    @Transactional
    public void delete(Long categoryId) {
        findById(categoryId);
        categoryRepository.deleteById(categoryId);
    }
}

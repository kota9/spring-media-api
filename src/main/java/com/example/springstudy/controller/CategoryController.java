package com.example.springstudy.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springstudy.dto.CategoryRequest;
import com.example.springstudy.dto.CategoryResponse;
import com.example.springstudy.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll().stream().map(CategoryResponse::from).toList();
    }

    @GetMapping("/{categoryId}")
    public CategoryResponse findById(@PathVariable Long categoryId) {
        return CategoryResponse.from(categoryService.findById(categoryId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        var created = categoryService.create(request);
        return ResponseEntity.created(URI.create("/api/categories/" + created.id()))
                .body(CategoryResponse.from(created));
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse update(
            @PathVariable Long categoryId, @Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(categoryService.update(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }
}

package com.example.springstudy.dto;

import com.example.springstudy.domain.Category;

public record CategoryResponse(Long id, String name, String description) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.id(), category.name(), category.description());
    }
}

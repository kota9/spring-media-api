package com.example.springstudy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContentCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters") String title,
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Author ID is required") Long authorId,
        @NotNull(message = "Category ID is required") Long categoryId) {
}

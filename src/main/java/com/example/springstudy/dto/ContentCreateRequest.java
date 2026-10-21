package com.example.springstudy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContentCreateRequest(
        @NotBlank String title,
        @NotNull String description) {
}

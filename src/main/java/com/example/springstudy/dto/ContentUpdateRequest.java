package com.example.springstudy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContentUpdateRequest(
        @NotBlank String title,
        @NotNull String description) {
}

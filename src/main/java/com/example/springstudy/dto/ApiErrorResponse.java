package com.example.springstudy.dto;

import java.time.LocalDateTime;

/** Stable response shape shared by all REST error handlers. */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        String path) {
}

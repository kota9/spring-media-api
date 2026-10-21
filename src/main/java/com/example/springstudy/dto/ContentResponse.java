package com.example.springstudy.dto;

import com.example.springstudy.domain.Content;

public record ContentResponse(Long id, String title, String description) {
    public static ContentResponse from(Content content) {
        return new ContentResponse(content.id(), content.title(), content.description());
    }
}

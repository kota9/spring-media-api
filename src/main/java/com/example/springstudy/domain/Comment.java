package com.example.springstudy.domain;

public record Comment(Long id, Long contentId, Long userId, String body) {
}

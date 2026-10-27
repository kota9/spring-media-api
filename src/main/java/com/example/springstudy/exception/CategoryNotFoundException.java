package com.example.springstudy.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long categoryId) {
        super("카테고리를 찾을 수 없습니다. id=" + categoryId);
    }
}

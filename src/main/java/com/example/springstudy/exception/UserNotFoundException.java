package com.example.springstudy.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("회원을 찾을 수 없습니다. id=" + userId);
    }
}

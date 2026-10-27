package com.example.springstudy.exception;

public class DuplicateUserEmailException extends RuntimeException {
    public DuplicateUserEmailException(String email) {
        super("이미 등록된 이메일입니다: " + email);
    }
}

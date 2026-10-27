package com.example.springstudy.exception;

import org.springframework.http.HttpStatus;

/** Central registry for stable application error identifiers and their HTTP status. */
public enum ErrorCode {
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "콘텐츠를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_USER_EMAIL", "이미 등록된 이메일입니다."),
    INVALID_CONTENT_PATCH(HttpStatus.BAD_REQUEST, "INVALID_CONTENT_PATCH", "콘텐츠 수정 요청이 잘못되었습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 데이터가 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
    public String message() { return message; }
}

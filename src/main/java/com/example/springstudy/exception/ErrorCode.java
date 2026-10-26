package com.example.springstudy.exception;

import org.springframework.http.HttpStatus;

/** Central registry for stable application error identifiers and their HTTP status. */
public enum ErrorCode {
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "콘텐츠를 찾을 수 없습니다."),
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

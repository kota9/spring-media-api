package com.example.springstudy.exception;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.example.springstudy.dto.ApiErrorResponse;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(HttpServletRequest request) {
        return error(ErrorCode.CONTENT_NOT_FOUND, request);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCategoryNotFound(HttpServletRequest request) {
        return error(ErrorCode.CATEGORY_NOT_FOUND, request);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(HttpServletRequest request) {
        return error(ErrorCode.USER_NOT_FOUND, request);
    }

    @ExceptionHandler(DuplicateUserEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUserEmail(HttpServletRequest request) {
        return error(ErrorCode.DUPLICATE_USER_EMAIL, request);
    }

    @ExceptionHandler(InvalidContentPatchException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPatch(HttpServletRequest request) {
        return error(ErrorCode.INVALID_CONTENT_PATCH, request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(HttpServletRequest request) {
        return error(ErrorCode.INVALID_REQUEST, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        ErrorCode code = status.is4xxClientError()
                ? ErrorCode.INVALID_REQUEST : ErrorCode.INTERNAL_SERVER_ERROR;
        return error(status, code, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error while handling {} {}", request.getMethod(), request.getRequestURI(), exception);
        return error(ErrorCode.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ApiErrorResponse> error(ErrorCode errorCode, HttpServletRequest request) {
        return error(errorCode.status(), errorCode, request);
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, ErrorCode errorCode, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(), status.value(), errorCode.code(), errorCode.message(), request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}

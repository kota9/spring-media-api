package com.example.springstudy.exception;

import java.util.Map;
import java.util.List;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            ContentNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidContentPatchException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPatch(
            InvalidContentPatchException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(
            ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return error(status, exception.getReason() == null ? status.getReasonPhrase() : exception.getReason(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            ConstraintViolationException.class})
    public ResponseEntity<?> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = "Invalid request";
        if (exception instanceof MethodArgumentNotValidException validationException) {
            Map<String, List<String>> errors = new TreeMap<>();
            validationException.getBindingResult().getFieldErrors().forEach(error ->
                    errors.computeIfAbsent(error.getField(), field -> new java.util.ArrayList<>())
                            .add(error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "VALIDATION_ERROR",
                    "message", "요청 값이 올바르지 않습니다.",
                    "errors", errors));
        } else if (exception instanceof HttpMessageNotReadableException) {
            message = "Request body is missing or malformed";
        }
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(Map.of(
                "status", String.valueOf(status.value()),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", request.getRequestURI()));
    }
}

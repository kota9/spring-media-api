package com.example.springstudy.exception;

/**
 * Unchecked because a missing entity is a business failure callers normally cannot
 * recover from locally; it propagates to the REST advice without checked-exception
 * declarations through every service and controller method.
 */
public class ContentNotFoundException extends RuntimeException {
    public ContentNotFoundException(Long id) {
        super("콘텐츠를 찾을 수 없습니다. id=" + id);
    }
}

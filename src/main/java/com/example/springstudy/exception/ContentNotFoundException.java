package com.example.springstudy.exception;

/**
 * Unchecked because a missing entity is a business failure callers normally cannot
 * recover from locally; it propagates to the REST advice without checked-exception
 * declarations through every service and controller method.
 */
public class ContentNotFoundException extends RuntimeException {
    public ContentNotFoundException(Long id) {
        super("Content not found: " + id);
    }
}

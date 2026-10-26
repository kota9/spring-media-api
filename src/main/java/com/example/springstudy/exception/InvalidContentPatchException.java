package com.example.springstudy.exception;

/** Unchecked business exception handled centrally by {@link ApiExceptionHandler}. */
public class InvalidContentPatchException extends RuntimeException {
    public InvalidContentPatchException(String message) {
        super(message);
    }
}

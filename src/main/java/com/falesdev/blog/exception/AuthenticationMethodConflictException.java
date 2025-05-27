package com.falesdev.blog.exception;

public class AuthenticationMethodConflictException extends RuntimeException {
    public AuthenticationMethodConflictException(String message) {
        super(message);
    }
}

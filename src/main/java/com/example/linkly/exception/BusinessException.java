package com.example.linkly.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String details;

    public BusinessException(String message) {
        super(message);
        this.details = null;
    }

    public BusinessException(String message, String details) {
        super(message);
        this.details = details;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.details = null;
    }
}

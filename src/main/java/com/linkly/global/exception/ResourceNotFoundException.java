package com.linkly.global.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s를 찾을 수 없습니다. (ID: %d)", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s를 찾을 수 없습니다. (%s)", resourceName, identifier));
    }
}
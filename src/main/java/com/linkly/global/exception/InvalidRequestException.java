package com.linkly.global.exception;

public class InvalidRequestException extends BusinessException {

	public InvalidRequestException(String message) {
		super(message);
	}

	public InvalidRequestException(String message, String details) {
		super(message, details);
	}
}

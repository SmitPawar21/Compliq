package com.smit.compliq.exception;

public class DocumentNotFoundException extends RuntimeException {
	public DocumentNotFoundException(String message) {
		super(message);
	}
}

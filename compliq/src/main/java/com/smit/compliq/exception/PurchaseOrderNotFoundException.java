package com.smit.compliq.exception;

public class PurchaseOrderNotFoundException extends RuntimeException {
	public PurchaseOrderNotFoundException(String message) {
		super(message);
	}
}

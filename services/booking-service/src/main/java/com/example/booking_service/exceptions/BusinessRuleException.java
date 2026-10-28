package com.example.booking_service.exceptions;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) { super(message); }
}

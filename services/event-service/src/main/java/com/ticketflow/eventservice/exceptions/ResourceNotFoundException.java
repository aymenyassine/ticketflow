package com.ticketflow.eventservice.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String type, Object id) {
        super(type + " " + id + " not found");
    }
}

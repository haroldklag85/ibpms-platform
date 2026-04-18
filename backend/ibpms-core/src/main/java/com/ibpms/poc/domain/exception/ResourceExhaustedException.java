package com.ibpms.poc.domain.exception;

public class ResourceExhaustedException extends RuntimeException {
    public ResourceExhaustedException(String message) {
        super(message);
    }
}

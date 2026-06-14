package com.ibpms.poc.domain.exception;

public class TaskOwnershipViolationException extends RuntimeException {
    public TaskOwnershipViolationException(String message) {
        super(message);
    }
}

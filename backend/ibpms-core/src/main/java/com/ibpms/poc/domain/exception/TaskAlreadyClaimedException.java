package com.ibpms.poc.domain.exception;

public class TaskAlreadyClaimedException extends RuntimeException {
    public TaskAlreadyClaimedException(String message) {
        super(message);
    }
}

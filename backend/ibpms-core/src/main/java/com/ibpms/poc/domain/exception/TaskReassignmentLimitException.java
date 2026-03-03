package com.ibpms.poc.domain.exception;

public class TaskReassignmentLimitException extends RuntimeException {
    public TaskReassignmentLimitException(String message) {
        super(message);
    }
}

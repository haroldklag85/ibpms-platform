package com.ibpms.poc.domain.exception;

import com.ibpms.poc.crosscutting.annotations.Traceability;

// @Traceability: US-003 - ADR-001 - Pure Domain Exception
@Traceability(US = "US-003", CA = {"CA-91"})
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}

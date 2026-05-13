package com.ibpms.poc.application.service.security.exceptions;

import java.util.List;

public class PreconditionRequiredException extends RuntimeException {
    
    private final List<String> missingFields;

    public PreconditionRequiredException(String message, List<String> missingFields) {
        super(message);
        this.missingFields = missingFields;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }
}

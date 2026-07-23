package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.service.security.exceptions.PreconditionRequiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(PreconditionRequiredException.class)
    public ResponseEntity<Map<String, Object>> handlePreconditionRequired(PreconditionRequiredException ex) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                .body(Map.of(
                        "error", "Precondition Required",
                        "message", ex.getMessage(),
                        "missing_fields", ex.getMissingFields()
                ));
    }
}

package com.ibpms.core.sac.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConnectionValidationException extends RuntimeException {

    public ConnectionValidationException(String message) {
        super(message);
    }

    public ConnectionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

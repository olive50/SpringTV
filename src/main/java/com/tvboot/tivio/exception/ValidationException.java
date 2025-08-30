package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class ValidationException extends TvBootException {
    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, validationErrors);
    }

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }
}
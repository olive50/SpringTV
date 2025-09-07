package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends TvbootException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";

    public ValidationException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, Object data) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST, data);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String field, String reason) {
        super(String.format("Validation failed for field '%s': %s", field, reason),
                ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
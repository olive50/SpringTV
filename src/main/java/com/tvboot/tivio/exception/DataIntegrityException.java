package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when data integrity is violated
 * Note: Spring's DataIntegrityViolationException is already handled in GlobalExceptionHandler
 * This is for custom data integrity checks
 */
public class DataIntegrityException extends TvbootException {

    private static final String ERROR_CODE = "DATA_INTEGRITY_ERROR";

    public DataIntegrityException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public DataIntegrityException(String message, Object data) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT, data);
    }

    public DataIntegrityException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
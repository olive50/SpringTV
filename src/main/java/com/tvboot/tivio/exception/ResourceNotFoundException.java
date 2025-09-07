package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends TvbootException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(message, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message, Object data) {
        super(message, ERROR_CODE, HttpStatus.NOT_FOUND, data);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                ERROR_CODE, HttpStatus.NOT_FOUND);
    }
}
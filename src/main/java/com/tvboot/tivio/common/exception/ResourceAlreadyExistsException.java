package com.tvboot.tivio.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource already exists
 */
public class ResourceAlreadyExistsException extends TvbootException {

    private static final String ERROR_CODE = "RESOURCE_ALREADY_EXISTS";

    public ResourceAlreadyExistsException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public ResourceAlreadyExistsException(String message, Object data) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT, data);
    }

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                ERROR_CODE, HttpStatus.CONFLICT);
    }
}
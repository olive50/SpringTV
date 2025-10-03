package com.tvboot.tivio.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an operation is not supported
 */
public class OperationNotSupportedException extends TvbootException {

    private static final String ERROR_CODE = "OPERATION_NOT_SUPPORTED";

    public OperationNotSupportedException(String message) {
        super(message, ERROR_CODE, HttpStatus.NOT_IMPLEMENTED);
    }

    public OperationNotSupportedException(String operation, String reason) {
        super(String.format("Operation '%s' is not supported: %s", operation, reason),
                ERROR_CODE, HttpStatus.NOT_IMPLEMENTED);
    }
}

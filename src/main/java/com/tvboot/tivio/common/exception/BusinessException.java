package com.tvboot.tivio.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for business logic violations
 */
public class BusinessException extends TvbootException {

    private static final String ERROR_CODE = "BUSINESS_ERROR";

    public BusinessException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, Object data) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST, data);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}

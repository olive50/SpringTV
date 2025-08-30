package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends TvBootException {
    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, Object data) {
        super(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST, data);
    }
}
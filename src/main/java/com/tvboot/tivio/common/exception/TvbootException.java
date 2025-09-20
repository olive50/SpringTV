package com.tvboot.tivio.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for TVBOOT IPTV system
 * All custom exceptions should extend this class
 */
public abstract class TvbootException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object data;

    /**
     * Constructor with message, error code, and HTTP status
     */
    protected TvbootException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = null;
    }

    /**
     * Constructor with message, error code, HTTP status, and additional data
     */
    protected TvbootException(String message, String errorCode, HttpStatus httpStatus, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = data;
    }

    /**
     * Constructor with message, cause, error code, and HTTP status
     */
    protected TvbootException(String message, Throwable cause, String errorCode, HttpStatus httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = null;
    }

    /**
     * Constructor with all parameters
     */
    protected TvbootException(String message, Throwable cause, String errorCode, HttpStatus httpStatus, Object data) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Object getData() {
        return data;
    }
}

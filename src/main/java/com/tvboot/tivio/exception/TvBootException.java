package com.tvboot.tivio.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TvBootException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object data;

    public TvBootException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.data = null;
    }

    public TvBootException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.data = null;
    }

    public TvBootException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = null;
    }

    public TvBootException(String message, String errorCode, HttpStatus httpStatus, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public TvBootException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.data = null;
    }

    public TvBootException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.data = null;
    }

    public TvBootException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = null;
    }
}
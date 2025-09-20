package com.tvboot.tivio.common.exception;


import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's an issue with external service integration
 */
public class ExternalServiceException extends TvbootException {

    private static final String ERROR_CODE = "EXTERNAL_SERVICE_ERROR";

    public ExternalServiceException(String message) {
        super(message, ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("External service '%s' error: %s", serviceName, message),
                ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ExternalServiceException(String serviceName, Throwable cause) {
        super(String.format("External service '%s' error", serviceName), cause,
                ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
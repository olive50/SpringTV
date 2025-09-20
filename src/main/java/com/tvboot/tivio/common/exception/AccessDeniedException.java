package com.tvboot.tivio.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when access is denied
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends TvbootException {

    private static final String ERROR_CODE = "ACCESS_DENIED";

    public AccessDeniedException(String message) {
        super(message, ERROR_CODE, HttpStatus.FORBIDDEN);
    }

    public AccessDeniedException(String resource, String action) {
        super(String.format("Access denied: Cannot %s %s", action, resource),
                ERROR_CODE, HttpStatus.FORBIDDEN);
    }
}
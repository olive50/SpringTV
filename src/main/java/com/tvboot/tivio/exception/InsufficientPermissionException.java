package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for insufficient permissions
 * Note: This is different from Spring Security's AccessDeniedException
 * Use this for business-level permission checks
 */
public class InsufficientPermissionException extends TvbootException {

    private static final String ERROR_CODE = "INSUFFICIENT_PERMISSION";

    public InsufficientPermissionException(String message) {
        super(message, ERROR_CODE, HttpStatus.FORBIDDEN);
    }

    public InsufficientPermissionException(String action, String resource) {
        super(String.format("Insufficient permission to %s %s", action, resource),
                ERROR_CODE, HttpStatus.FORBIDDEN);
    }
}

package com.tvboot.tivio.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when rate limit is exceeded
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends TvbootException {

    private static final String ERROR_CODE = "RATE_LIMIT_EXCEEDED";

    public RateLimitExceededException(String message) {
        super(message, ERROR_CODE, HttpStatus.TOO_MANY_REQUESTS);
    }

    public RateLimitExceededException(int limit, String period) {
        super(String.format("Rate limit exceeded: Maximum %d requests per %s", limit, period),
                ERROR_CODE, HttpStatus.TOO_MANY_REQUESTS);
    }
}
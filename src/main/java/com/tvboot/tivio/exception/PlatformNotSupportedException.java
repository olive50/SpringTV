package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when platform is not supported
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PlatformNotSupportedException extends TvbootException {

    private static final String ERROR_CODE = "PLATFORM_NOT_SUPPORTED";

    public PlatformNotSupportedException(String platform) {
        super(String.format("Platform '%s' is not supported", platform),
                ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public PlatformNotSupportedException(String platform, String languageCode) {
        super(String.format("Platform '%s' is not supported for language '%s'", platform, languageCode),
                ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
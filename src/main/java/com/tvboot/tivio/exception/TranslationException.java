package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when translation operation fails
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TranslationException extends TvbootException {

    private static final String ERROR_CODE = "TRANSLATION_ERROR";

    public TranslationException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public TranslationException(String languageCode, String reason) {
        super(String.format("Translation error for language '%s': %s", languageCode, reason),
                ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
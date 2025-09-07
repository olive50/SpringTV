package com.tvboot.tivio.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Standard error response DTO for all API errors
 * Used by GlobalExceptionHandler
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    private String timestamp;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * HTTP status reason phrase
     */
    private String error;

    /**
     * User-friendly error message
     */
    private String message;

    /**
     * Application-specific error code
     */
    private String errorCode;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Unique trace ID for debugging
     */
    private String traceId;

    /**
     * Additional data related to the error (optional)
     */
    private Object data;

    /**
     * Validation errors for field-level validation failures
     */
    private List<ValidationError> validationErrors;

    /**
     * Nested class for validation error details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationError {
        /**
         * Field name that failed validation
         */
        private String field;

        /**
         * The value that was rejected
         */
        private Object rejectedValue;

        /**
         * Validation error message
         */
        private String message;

        /**
         * Validation error code
         */
        private String code;
    }
}
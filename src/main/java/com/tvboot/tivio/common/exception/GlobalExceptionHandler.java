package com.tvboot.tivio.common.exception;

import com.tvboot.tivio.auth.dto.MessageResponse;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.validation.BindingResultUtils.getBindingResult;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TvbootException.class)
    public ResponseEntity<ErrorResponse> handleTvBootException(
            TvbootException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.error("TvBootException occurred: {} | TraceId: {}", ex.getMessage(), traceId, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(ex.getHttpStatus().value())
                .error(ex.getHttpStatus().getReasonPhrase())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .path(request.getRequestURI())
                .traceId(traceId)
                .data(ex.getData())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        log.warn("Validation failed: {} errors | TraceId: {}", validationErrors.size(), traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .errorCode("VALIDATION_ERROR")
                .path(request.getRequestURI())
                .traceId(traceId)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(this::mapConstraintViolation)
                .collect(Collectors.toList());

        log.warn("Constraint violation: {} violations | TraceId: {}", validationErrors.size(), traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Constraint validation failed")
                .errorCode("CONSTRAINT_VIOLATION")
                .path(request.getRequestURI())
                .traceId(traceId)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(SQLGrammarException.class)
    public ResponseEntity<ErrorResponse> handleSQLGrammarException(SQLGrammarException ex, HttpServletRequest request) {
        String message = "Database query error: invalid column or type mismatch";

        var cause = ex.getMessage();
        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .error("Database query")
                .message(message)
                .errorCode("query_error")
                .path(request.getRequestURI())
                .traceId(traceId)
                .data(cause)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

    }
    /**
     * Handles TerminalNotActivatedException and maps it to HTTP 403 Forbidden.
     * This is an authorization/licensing issue.
     */
    @ExceptionHandler(TerminalNotActivatedException.class)
    public ResponseEntity<ErrorResponse> handleTerminalNotActivatedException(
            TerminalNotActivatedException ex,
            HttpServletRequest request) {

        // Define the appropriate status code and message
        HttpStatus status = HttpStatus.FORBIDDEN; // 403 Forbidden is a good fit for licensing/activation issues

        // Build the ErrorResponse DTO using the provided structure
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("The terminal is not activated. Operation is forbidden. Please check your activation status.")
                .errorCode("TIVIO-001") // Custom application error code
                .path(request.getRequestURI())
                // traceId can be added here if you use a tracing framework like Sleuth/Micrometer
                .build();

        // Return the structured response entity
        return new ResponseEntity<>(errorResponse, status);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.error("Data integrity violation | TraceId: {}", traceId, ex);

        String message = "Data integrity constraint violation";
        if (ex.getMessage().contains("Duplicate entry")) {
            message = "Duplicate entry - resource already exists";
        } else if (ex.getMessage().contains("foreign key constraint")) {
            message = "Cannot delete - resource is referenced by other entities";
        }

        var cause =  ex.getMessage();


        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message(message)
                .errorCode("DATA_INTEGRITY_ERROR")
                .path(request.getRequestURI())
                .traceId(traceId)
                .data(cause)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.warn("Access denied: {} | TraceId: {}", ex.getMessage(), traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("Insufficient permissions to access this resource")
                .errorCode("ACCESS_DENIED")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    // AJOUTEZ CETTE MÉTHODE POUR GÉRER IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.warn("IllegalArgumentException occurred: {} | TraceId: {}", ex.getMessage(), traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message(ex.getMessage()) // ← Affiche le vrai message d'erreur
                .errorCode("VALIDATION_ERROR")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }



    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.warn("Invalid JSON format | TraceId: {}", traceId, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request")
                .message("Invalid JSON format or malformed request body")
                .errorCode("INVALID_JSON")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex,
            HttpServletRequest request
    ) {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("No endpoint " + ex.getHttpMethod() + " " + ex.getRequestURL())
                .errorCode("ENDPOINT_NOT_FOUND")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.warn("Bad credentials | TraceId: {}", traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Invalid username or password")   // 👈 Correct message
                .errorCode("BAD_CREDENTIALS")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(
            DisabledException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.warn("Disabled account | TraceId: {}", traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Account is disabled")
                .errorCode("ACCOUNT_DISABLED")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(
            ExpiredJwtException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.warn("Expired JWT | TraceId: {}", traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Token has expired. Please login again.")
                .errorCode("TOKEN_EXPIRED")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.warn("Authentication failed: {} | TraceId: {}", ex.getMessage(), traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message("Authentication error")   // 👈 Only for other AuthenticationException
                .errorCode("AUTHENTICATION_ERROR")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }



    @ExceptionHandler(UnauthorizedTerminalException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedTerminalException(
            UnauthorizedTerminalException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.warn("Authentication failed: {} | TraceId: {}", ex.getMessage(), traceId);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message("Unauthorized TerminalException")   // 👈 Only for other AuthenticationException
                .errorCode("AUTHENTICATION_ERROR")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        log.error("Unexpected error occurred | TraceId: {}", traceId, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please contact support with trace ID: " + traceId)
                .errorCode("INTERNAL_ERROR")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ErrorResponse.ValidationError mapFieldError(FieldError fieldError) {
        return ErrorResponse.ValidationError.builder()
                .field(fieldError.getField())
                .rejectedValue(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .code(fieldError.getCode())
                .build();
    }

    private ErrorResponse.ValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        return ErrorResponse.ValidationError.builder()
                .field(violation.getPropertyPath().toString())
                .rejectedValue(violation.getInvalidValue())
                .message(violation.getMessage())
                .code(violation.getMessageTemplate())
                .build();
    }

// ==========================================
// HELPER METHODS
// ==========================================

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

}
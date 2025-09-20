package com.tvboot.tivio.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime timeStamp;

    protected int statusCode;
    protected HttpStatus status;
    protected String reason;
    protected String message;
    protected String developerMessage;
    protected Map<String, Object> data;

    // Your original helper methods
    public static ApiResponseBuilder<?, ?> success() {
        return builder()
                .statusCode(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .reason(HttpStatus.OK.getReasonPhrase())
                .timeStamp(LocalDateTime.now());
    }

    public static ApiResponseBuilder<?, ?> error(HttpStatus status, String message) {
        return builder()
                .statusCode(status.value())
                .status(status)
                .reason(status.getReasonPhrase())
                .message(message)
                .timeStamp(LocalDateTime.now());
    }

    // Enhanced helper methods for common HTTP statuses
    public static ApiResponseBuilder<?, ?> created() {
        return builder()
                .statusCode(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .reason(HttpStatus.CREATED.getReasonPhrase())
                .timeStamp(LocalDateTime.now());
    }

    public static ApiResponseBuilder<?, ?> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiResponseBuilder<?, ?> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }

    public static ApiResponseBuilder<?, ?> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    public static ApiResponseBuilder<?, ?> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message);
    }

    public static ApiResponseBuilder<?, ?> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    // Your addData method - perfect as is
    public ApiResponse addData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
        return this;
    }

    // Additional convenience methods for IPTV domain
    public ApiResponse addChannel(Object channel) {
        return addData("channel", channel);
    }

    public ApiResponse addChannels(List<?> channels) {
        return addData("channels", channels);
    }

    public ApiResponse addStream(Object stream) {
        return addData("stream", stream);
    }

    public ApiResponse addStreams(List<?> streams) {
        return addData("streams", streams);
    }

    public ApiResponse addHotel(Object hotel) {
        return addData("hotel", hotel);
    }

    public ApiResponse addUser(Object user) {
        return addData("user", user);
    }

    public ApiResponse addCount(long count) {
        return addData("count", count);
    }

    public ApiResponse addPagination(int page, int size, long total) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("total", total);
        pagination.put("totalPages", (int) Math.ceil((double) total / size));
        return addData("pagination", pagination);
    }

    // Static factory methods that return ResponseEntity directly
    public static ResponseEntity<ApiResponse> ok(String message) {
        ApiResponse response = ApiResponse.success()
                .message(message)
                .build();
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<ApiResponse> ok(String message, String key, Object value) {
        ApiResponse response = ApiResponse.success()
                .message(message)
                .build()
                .addData(key, value);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<ApiResponse> okWithChannels(String message, List<?> channels) {
        ApiResponse response = ApiResponse.success()
                .message(message)
                .build()
                .addChannels(channels);
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<ApiResponse> createdWithChannel(String message, Object channel) {
        ApiResponse response = ApiResponse.created()
                .message(message)
                .build()
                .addChannel(channel);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static ResponseEntity<ApiResponse> badRequestResponse(String message) {
        ApiResponse response = ApiResponse.badRequest(message).build();
        return ResponseEntity.badRequest().body(response);
    }

    public static ResponseEntity<ApiResponse> notFoundResponse(String message) {
        ApiResponse response = ApiResponse.notFound(message).build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    public static ResponseEntity<ApiResponse> internalServerErrorResponse(String message, String developerMessage) {
        ApiResponse response = ApiResponse.internalServerError(message)
                .developerMessage(developerMessage)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
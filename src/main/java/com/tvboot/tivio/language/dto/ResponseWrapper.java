package com.tvboot.tivio.language.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
        * API Response wrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> {
    private boolean success;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> ResponseWrapper<T> success(T data) {
        return ResponseWrapper.<T>builder()
                .success(true)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ResponseWrapper<T> success(T data, String message) {
        return ResponseWrapper.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ResponseWrapper<T> error(String message) {
        return ResponseWrapper.<T>builder()
                .success(false)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}


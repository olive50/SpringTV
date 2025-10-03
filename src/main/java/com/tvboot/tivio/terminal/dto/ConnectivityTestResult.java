package com.tvboot.tivio.terminal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectivityTestResult {
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;
    private String errorCode;
    private Map<String, Object> details;
}
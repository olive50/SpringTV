package com.tvboot.tivio.media.stream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamHealthResult {
    private Long channelId;
    private String channelName;
    private Integer channelNumber;
    private String streamUrl;
    private String multicastAddress;
    private Integer port;
    private Boolean available;
    private String status; // ONLINE, OFFLINE, ERROR
    private String message;
    private LocalDateTime lastChecked;
    private Long responseTimeMs;
}
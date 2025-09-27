package com.tvboot.tivio.terminal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalConnectivityDto {
    private String terminalCode;
    private Boolean isOnline;
    private Double uptime;
    private LocalDateTime lastSeen;
    private String ipAddress;
    private String macAddress;
}

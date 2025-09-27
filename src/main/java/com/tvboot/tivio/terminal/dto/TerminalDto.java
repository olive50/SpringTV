package com.tvboot.tivio.terminal.dto;

import com.tvboot.tivio.common.enumeration.DeviceType;
import com.tvboot.tivio.common.enumeration.TerminalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalDto {
    private Long id;
    private String terminalCode;
    private DeviceType deviceType;
    private String brand;
    private String model;
    private String macAddress;
    private String ipAddress;
    private TerminalStatus status;
    private String location;
    private String RoomNumber;
    private LocalDateTime lastSeen;
    private String firmwareVersion;
    private String serialNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Connection info
    private Double uptime;
    private Boolean isOnline;
}
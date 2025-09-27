package com.tvboot.tivio.terminal.dto;

import com.tvboot.tivio.common.enumeration.DeviceType;
import com.tvboot.tivio.common.enumeration.TerminalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalUpdateRequest {
    private String terminalCode;
    private DeviceType deviceType;
    private String brand;
    private String model;
    private String macAddress;
    private String ipAddress;
    private TerminalStatus status;
    private String location;
    private Long roomId;
    private String firmwareVersion;
    private String serialNumber;
}
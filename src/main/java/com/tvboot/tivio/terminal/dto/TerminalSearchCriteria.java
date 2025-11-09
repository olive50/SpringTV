package com.tvboot.tivio.terminal.dto;

import com.tvboot.tivio.common.enumeration.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminalSearchCriteria {
    private String search;
    private Boolean active;
    private DeviceType deviceType;
    private String locationId;

    private String terminalNumber;

    private Integer floorNumber;

    // Additional search criteria if needed
    private Boolean isActive;
    private String macAddress;
    private String ipAddress;
}
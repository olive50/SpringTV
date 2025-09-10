package com.tvboot.tivio.terminal.dto;

import com.tvboot.tivio.terminal.DeviceType;
import com.tvboot.tivio.terminal.TerminalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminalSearchCriteria {
    private String search;
    private TerminalStatus status;
    private DeviceType deviceType;
    private String location;


    private Long roomId;
    private String terminalNumber;


    private String building;
    private Integer floorNumber;

    // Additional search criteria if needed
    private Boolean isActive;
    private String macAddress;
    private String ipAddress;
}
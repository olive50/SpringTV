package com.tvboot.tivio.terminal.dto;

import com.tvboot.tivio.common.enumeration.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalCreateRequest {
    @NotBlank(message = "Terminal ID is required")
    @Size(min = 3, max = 20, message = "Terminal ID must be between 3 and 20 characters")
    private String terminalCode;

    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "Invalid MAC address format")
    private String macAddress;

    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "Invalid IP address format")
    private String ipAddress;

    @NotBlank(message = "LocationType is required")
    private String locationType;

    @NotBlank(message = "Location identifier is required")
    private String locationIdentifier;
    private Boolean active=false;
    private String platform;
    private String appVersion;
    private String firmwareVersion;
    private String comment;
}
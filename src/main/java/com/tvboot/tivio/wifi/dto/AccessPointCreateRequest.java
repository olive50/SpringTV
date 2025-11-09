package com.tvboot.tivio.wifi.dto;

import com.tvboot.tivio.common.enumeration.AccessPointType;
import com.tvboot.tivio.common.enumeration.WifiSecurityProtocol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccessPointCreateRequest {

    @NotBlank(message = "SSID is required")
    @Size(max = 32, message = "SSID must not exceed 32 characters")
    private String ssid;

    @Size(max = 63, message = "Password must not exceed 63 characters")
    private String password;

    @NotNull(message = "Security protocol is required")
    private WifiSecurityProtocol securityProtocol;

    private Boolean available;

    private Boolean enabled;

    @NotNull(message = "Access point type is required")
    private AccessPointType type;

    @NotNull(message = "Terminal ID is required")
    private Long terminalId;
}
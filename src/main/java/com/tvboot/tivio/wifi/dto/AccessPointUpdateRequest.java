package com.tvboot.tivio.wifi.dto;

import com.tvboot.tivio.common.enumeration.AccessPointType;
import com.tvboot.tivio.common.enumeration.WifiSecurityProtocol;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccessPointUpdateRequest {

    @Size(max = 32, message = "SSID must not exceed 32 characters")
    private String ssid;

    @Size(max = 63, message = "Password must not exceed 63 characters")
    private String password;

    private WifiSecurityProtocol securityProtocol;

    private Boolean available;

    private Boolean enabled;

    private AccessPointType type;
}
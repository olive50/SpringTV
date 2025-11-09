package com.tvboot.tivio.wifi.dto;

import com.tvboot.tivio.common.enumeration.AccessPointType;
import com.tvboot.tivio.common.enumeration.WifiSecurityProtocol;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessPointDTO {
    private Long id;
    private String ssid;
    private String password;
    private WifiSecurityProtocol securityProtocol;
    private Boolean available;
    private Boolean enabled;
    private AccessPointType type;
    private Long terminalId;
}
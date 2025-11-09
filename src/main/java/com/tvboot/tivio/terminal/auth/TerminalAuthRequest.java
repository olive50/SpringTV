package com.tvboot.tivio.terminal.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TerminalAuthRequest {
    @NotBlank
    private String macAddress;

    @NotBlank
    private String terminalCode; // DUID for Samsung

    private String ipAddress; // Set by controller

    @NotBlank
    private String appVersion;

    private String firmwareVersion;
}

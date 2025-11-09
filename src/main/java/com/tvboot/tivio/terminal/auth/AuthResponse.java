package com.tvboot.tivio.terminal.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private Long terminalId;
}
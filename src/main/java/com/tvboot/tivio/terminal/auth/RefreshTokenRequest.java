package com.tvboot.tivio.terminal.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // Optionnel : pour tracer quel terminal fait la requête
    private String macAddress;

    // Optionnel : pour mise à jour de l'IP
    private String ipAddress;
}
package com.tvboot.tivio.terminal.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/terminal")
@RequiredArgsConstructor
public class TerminalAuthController {

    private final TerminalAuthenticationService authService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(
            @RequestBody @Valid TerminalAuthRequest request,
            HttpServletRequest httpRequest) {

        // Ajouter l'IP réelle du terminal
        String ipAddress = getClientIp(httpRequest);
        request.setIpAddress(ipAddress);

        AuthResponse response = authService.authenticateTerminal(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshTerminalToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
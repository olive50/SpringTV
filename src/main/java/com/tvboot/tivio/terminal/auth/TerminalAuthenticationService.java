package com.tvboot.tivio.terminal.auth;

import com.tvboot.tivio.common.exception.TerminalNotActivatedException;
import com.tvboot.tivio.common.exception.UnauthorizedTerminalException;
import com.tvboot.tivio.terminal.Terminal;
import com.tvboot.tivio.terminal.TerminalRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TerminalAuthenticationService {

    private final TerminalRepository terminalRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.terminal.expiration:7776000}") // 90 jours
    private long terminalTokenExpiration;

    public AuthResponse authenticateTerminal(TerminalAuthRequest request) {
        // Validation du terminal
        Terminal terminal = terminalRepository
                .findByMacAddressAndTerminalCode(
                        request.getMacAddress(),
                        request.getTerminalCode()
                )
                .orElseThrow(() -> new UnauthorizedTerminalException("Invalid terminal credentials"));

        // Vérifier si le terminal est actif
        if (!terminal.getActive()) {
            throw new TerminalNotActivatedException("Terminal not activated");
        }

        // Mise à jour statut
        terminal.setLastSeen(LocalDateTime.now());
        terminal.setIsOnline(true);
        terminal.setIpAddress(request.getIpAddress());
        terminalRepository.save(terminal);

        // Génération du token avec rôle TERMINAL
        String token = jwtTokenProvider.createTerminalToken(
                terminal.getTerminalCode(),
                terminal.getId(),
                terminalTokenExpiration
        );

        return AuthResponse.builder()
                .token(token)
                .refreshToken(generateRefreshToken(terminal))
                .expiresIn(terminalTokenExpiration)
                .terminalId(terminal.getId())
                .build();
    }

    private String generateRefreshToken(Terminal terminal) {
        // Token de refresh avec durée plus longue (1 an)
        return jwtTokenProvider.createRefreshToken(
                terminal.getTerminalCode(),
                31536000L // 365 jours
        );
    }

    // ===========================
    // REFRESH TOKEN METHOD
    // ===========================
    public AuthResponse refreshTerminalToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new UnauthorizedTerminalException("Invalid or expired refresh token");
        }

        // Extract terminal code from the refresh token
        String terminalCode = jwtTokenProvider.getTerminalCodeFromRefreshToken(refreshToken);

        // Find the terminal by code
        Terminal terminal = terminalRepository.findByTerminalCode(terminalCode)
                .orElseThrow(() -> new UnauthorizedTerminalException("Terminal not found"));

        if (!Boolean.TRUE.equals(terminal.getActive())) {
            throw new TerminalNotActivatedException("Terminal not activated");
        }

        // Generate a new access token (and optionally a new refresh token)
        String newAccessToken = jwtTokenProvider.createTerminalToken(
                terminal.getTerminalCode(),
                terminal.getId(),
                terminalTokenExpiration
        );

        String newRefreshToken = jwtTokenProvider.createRefreshToken(
                terminal.getTerminalCode(),
                31536000L // 1 year again
        );

        // Update terminal's last seen timestamp
        terminal.setLastSeen(LocalDateTime.now());
        terminalRepository.save(terminal);

        // Return new tokens
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(terminalTokenExpiration)
                .terminalId(terminal.getId())
                .build();
    }
}
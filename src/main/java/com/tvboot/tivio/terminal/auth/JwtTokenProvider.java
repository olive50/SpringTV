package com.tvboot.tivio.terminal.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.refresh.secret}")
    private String refreshSecret;

    private SecretKey getJwtSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshSecretKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    // =========================================================
    // TERMINAL TOKEN METHODS
    // =========================================================
    public String createTerminalToken(String terminalCode, Long terminalId, long expirationInSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInSeconds * 1000);

        return Jwts.builder()
                .subject(terminalCode)
                .claim("terminalId", terminalId)
                .claim("type", "TERMINAL")
                .claim("authorities", List.of("ROLE_TERMINAL"))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getJwtSecretKey())
                .compact();
    }

    // =========================================================
    // REFRESH TOKEN METHODS
    // =========================================================
    public String createRefreshToken(String terminalCode, long expirationInSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInSeconds * 1000);

        return Jwts.builder()
                .subject(terminalCode)
                .claim("type", "REFRESH")
                .claim("tokenType", "TERMINAL_REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getRefreshSecretKey())
                .compact();
    }

    // =========================================================
    // VALIDATION METHODS
    // =========================================================
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getJwtSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        } catch (Exception ex) {
            log.error("JWT validation error: {}", ex.getMessage());
        }
        return false;
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser()
                    .verifyWith(getRefreshSecretKey())
                    .build()
                    .parseSignedClaims(refreshToken);
            return true;
        } catch (Exception ex) {
            log.error("Invalid refresh token: {}", ex.getMessage());
        }
        return false;
    }

    // =========================================================
    // EXTRACTION METHODS
    // =========================================================
    public Claims parseTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(getJwtSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getTerminalCodeFromToken(String token) {
        return parseTokenClaims(token).getSubject();
    }
    public String getTerminalCodeFromRefreshToken(String token) {
        return parseTokenClaims(token).getSubject();
    }

    public Long getTerminalIdFromToken(String token) {
        return parseTokenClaims(token).get("terminalId", Long.class);
    }

    public boolean isTerminalToken(String token) {
        try {
            return "TERMINAL".equals(parseTokenClaims(token).get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getRefreshSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "REFRESH".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // EXPIRATION METHODS
    // =========================================================
    public Date getExpirationDateFromToken(String token) {
        return parseTokenClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

package com.tvboot.tivio.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties spécifiques à la production
 */
@Data
@Component
@Profile("prod")
@ConfigurationProperties(prefix = "app.security.production")
public class ProductionSecurityConfig {

    /**
     * Origins CORS autorisés en production
     */
    private List<String> allowedOrigins = List.of(
            "https://tvboot.com",
            "https://*.tvboot.com",
            "https://admin.tvboot.com"
    );

    /**
     * Endpoints sensibles nécessitant une authentification renforcée
     */
    private List<String> criticalEndpoints = List.of(
            "/api/users/**",
            "/api/auth/register",
            "/api/languages/initialize",
            "/api/languages/import"
    );

    /**
     * Configuration JWT pour la production
     */
    private JwtConfig jwt = new JwtConfig();

    /**
     * Configuration de session pour la production
     */
    private SessionConfig session = new SessionConfig();

    @Data
    public static class JwtConfig {
        private int expirationMs = 3600000; // 1 heure en prod
        private int refreshExpirationMs = 86400000; // 24 heures
        private boolean strictValidation = true;
    }

    @Data
    public static class SessionConfig {
        private int maxConcurrentSessions = 3;
        private boolean preventSessionFixation = true;
        private int sessionTimeoutMinutes = 30;
    }
}
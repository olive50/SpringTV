package com.tvboot.tivio.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration spécifique pour les tests
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Encoder plus rapide pour les tests
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        // Utiliser une force plus faible pour les tests (plus rapide)
        return new BCryptPasswordEncoder(4);
    }
}
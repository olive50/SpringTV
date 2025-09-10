package com.tvboot.tivio.config;

import com.tvboot.tivio.security.JwtAuthenticationFilter;
import com.tvboot.tivio.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionHandler securityExceptionHandler;

    // List of public endpoints for better maintainability
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/public/**",
            "/api/test/public",
            "/api/test/health",
            "/api/test/debug-users", // Temporary debug endpoint
            "/h2-console/**",
            "/error",

            // Swagger/OpenAPI endpoints
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-resources/**",
            "/webjars/**",


            // tomporaire kan for test

            "/api/hotel/**",
            "/api/channels/**",
            "/api/languages/**",
            "/api/terminals/**",
            "/api/rooms/**",
            "/api/guests/**"

    };

    // Admin-only endpoints
    private static final String[] ADMIN_ENDPOINTS = {
            "/api/auth/register",
            "/api/test/admin"
    };

    // Authenticated endpoints
    private static final String[] AUTHENTICATED_ENDPOINTS = {
            "/api/auth/me",
            "/api/test/protected"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS...");
        CorsConfiguration configuration = new CorsConfiguration();

        // Environment-specific origins
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*:*" // For local network access
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "X-Total-Count"));

        // Set max age for preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain...");

        // Log all the endpoints being configured
        log.info("Public endpoints: {}", String.join(", ", PUBLIC_ENDPOINTS));
        log.info("Admin endpoints: {}", String.join(", ", ADMIN_ENDPOINTS));
        log.info("Authenticated endpoints: {}", String.join(", ", AUTHENTICATED_ENDPOINTS));

        return http
                .cors(cors -> {
                    log.info("Applying CORS configuration");
                    cors.configurationSource(corsConfigurationSource());
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> {
                    log.info("Setting session management to STATELESS");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(authz -> {
                    log.info("Configuring authorization rules...");
                    authz
                            // Public endpoints - Order is important!
                            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                            // Admin-only endpoints
                            .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")

                            // Authenticated endpoints
                            .requestMatchers(AUTHENTICATED_ENDPOINTS).authenticated()

                            // All other API endpoints require authentication
                            .requestMatchers("/api/**").authenticated()

                            // Permit other requests (like static resources, actuator endpoints, etc.)
                            .anyRequest().permitAll(); // Changed from denyAll to permitAll to avoid blocking valid requests
                })
                .exceptionHandling(exceptions -> {
                    log.info("Configuring exception handling");
                    exceptions
                            .authenticationEntryPoint(securityExceptionHandler)
                            .accessDeniedHandler(securityExceptionHandler);
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // For H2 console in development
                //.headers(headers -> headers.frameOptions().disable())
                .build();
    }
}
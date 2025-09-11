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

    /**
     * Public endpoints that don't require authentication
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/public/**",
            "/api/test/public",
            "/api/test/health",
            "/api/test/info",
            "/api/admin/users/**", // Allow user verification endpoints (remove in production)
            "/h2-console/**",
            "/error",

            // Swagger/OpenAPI endpoints
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-resources/**",
            "/webjars/**",

            // Static resources
            "/uploads/**",
            "/assets/**"
    };

    /**
     * Admin-only endpoints
     */
    private static final String[] ADMIN_ENDPOINTS = {
            "/api/auth/register",
            "/api/users/admin/**",
            "/api/test/admin"
    };

    /**
     * Manager and above endpoints
     */
    private static final String[] MANAGER_ENDPOINTS = {
            "/api/languages/initialize",
            "/api/languages/import",
            "/api/languages/*/delete",
            "/api/packages/create",
            "/api/packages/*/delete"
    };

    /**
     * Authenticated endpoints (all authenticated users)
     */
    private static final String[] AUTHENTICATED_ENDPOINTS = {
            "/api/auth/me",
            "/api/test/protected"
    };

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Creating BCryptPasswordEncoder bean...");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12); // Strength 12 for better security
        log.info("BCryptPasswordEncoder created with strength 12");
        return encoder;
    }

    /**
     * Authentication provider configuration
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        log.info("Configuring DaoAuthenticationProvider...");

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); // Show user not found errors for debugging

        log.info("DaoAuthenticationProvider configured successfully");
        return authProvider;
    }

    /**
     * Authentication manager configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("Creating AuthenticationManager...");
        AuthenticationManager manager = config.getAuthenticationManager();
        log.info("AuthenticationManager created successfully");
        return manager;
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS...");

        CorsConfiguration configuration = new CorsConfiguration();

        // Environment-specific origins
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*:*", // Local network access
                "https://tvboot.com",
                "https://*.tvboot.com"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "X-Total-Count"));
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configuration completed");
        return source;
    }

    /**
     * Main security filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain...");

        // Log endpoint configuration for debugging
        log.info("Public endpoints: {}", String.join(", ", PUBLIC_ENDPOINTS));
        log.info("Admin endpoints: {}", String.join(", ", ADMIN_ENDPOINTS));
        log.info("Manager endpoints: {}", String.join(", ", MANAGER_ENDPOINTS));
        log.info("Authenticated endpoints: {}", String.join(", ", AUTHENTICATED_ENDPOINTS));

        return http
                .cors(cors -> {
                    log.debug("Applying CORS configuration");
                    cors.configurationSource(corsConfigurationSource());
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> {
                    log.debug("Setting session management to STATELESS");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(authz -> {
                    log.info("Configuring authorization rules...");
                    authz
                            // Public endpoints - No authentication required
                            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                            // Admin-only endpoints
                            .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")

                            // Manager and above endpoints
                            .requestMatchers(MANAGER_ENDPOINTS).hasAnyRole("ADMIN", "MANAGER")

                            // Channel management - Manager and above can create/modify, others can view
                            .requestMatchers("GET", "/api/channels/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN", "RECEPTIONIST")
                            .requestMatchers("POST", "/api/channels/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")
                            .requestMatchers("PUT", "/api/channels/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")
                            .requestMatchers("DELETE", "/api/channels/**").hasAnyRole("ADMIN", "MANAGER")

                            // Terminal management - Technician and above
                            .requestMatchers("/api/terminals/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")

                            // Room management - Receptionist and above
                            .requestMatchers("/api/rooms/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Guest management - Receptionist and above
                            .requestMatchers("/api/guests/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Reservation management - Receptionist and above
                            .requestMatchers("/api/reservations/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Language management - Admin can modify, others can view
                            .requestMatchers("GET", "/api/languages/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN", "RECEPTIONIST")
                            .requestMatchers("POST", "/api/languages/**").hasRole("ADMIN")
                            .requestMatchers("PUT", "/api/languages/**").hasRole("ADMIN")
                            .requestMatchers("DELETE", "/api/languages/**").hasRole("ADMIN")

                            // Category management - Manager and above
                            .requestMatchers("GET", "/api/categories/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN", "RECEPTIONIST")
                            .requestMatchers("POST", "/api/categories/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("PUT", "/api/categories/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("DELETE", "/api/categories/**").hasRole("ADMIN")

                            // Package management - Manager and above
                            .requestMatchers("/api/packages/**").hasAnyRole("ADMIN", "MANAGER")

                            // Hotel information - All authenticated users
                            .requestMatchers("/api/hotel/**").authenticated()

                            // Profile management - All authenticated users
                            .requestMatchers(AUTHENTICATED_ENDPOINTS).authenticated()

                            // All other API endpoints require authentication
                            .requestMatchers("/api/**").authenticated()

                            // Permit other requests (like static resources)
                            .anyRequest().permitAll();
                })
                .exceptionHandling(exceptions -> {
                    log.info("Configuring security exception handling");
                    exceptions
                            .authenticationEntryPoint(securityExceptionHandler)
                            .accessDeniedHandler(securityExceptionHandler);
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
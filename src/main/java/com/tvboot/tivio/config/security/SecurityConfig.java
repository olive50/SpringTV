package com.tvboot.tivio.config.security;

import com.tvboot.tivio.auth.UserDetailsServiceImpl;
import com.tvboot.tivio.common.enumeration.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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
            "/api/v1/auth/login",
            "/api/v1/auth/terminal/**",
            "/api/v1/public/**",
            "/api/v1/test/public",
            "/api/v1/test/health",
            "/api/v1/test/info",
            "/h2-console/**",
            "/error",
            "/api/v1/checkin/room-status/**", // arevoir
            "/api/v1/checkin/room/**", //just test

            // Swagger/OpenAPI endpoints
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-resources/**",
            "/webjars/**",

            // Static resources
            "/uploads/**",
            "/assets/**",

            //temporaire kan
            "/api/v1/tvchannels/**",
            "/api/v1/terminals/**",
            "/api/v1/files/**",
            //for tizen app momentane
            "/api/v1/hotel/**",
            "/api/v1/languages/**",
            "/api/v1/translations/**",

    };

    /**
     * Admin-only endpoints
     */
    private static final String[] ADMIN_ENDPOINTS = {
            "/api/v1/auth/register",
            "/api/v1/users/admin/**",
            "/api/v1/test/admin"
    };

    /**
     * Manager and above endpoints
     */
    private static final String[] MANAGER_ENDPOINTS = {
            "/api/v1/languages/initialize",
            "/api/v1/languages/import",
            "/api/v1/languages/*/delete",
            "/api/v1/packages/create",
            "/api/v1/packages/*/delete"
    };

    /**
     * Authenticated endpoints (all authenticated users)
     */
    private static final String[] AUTHENTICATED_ENDPOINTS = {
            "/api/v1/auth/me",
            "/api/v1/test/protected"
    };

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return  new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication provider configuration
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.info("Configuring DaoAuthenticationProvider...");

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
//        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        //authProvider.setHideUserNotFoundExceptions(false); // Show user not found errors for debugging

//        log.info("DaoAuthenticationProvider configured successfully");
        return authProvider;
    }

    /**
     * Authentication manager configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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
                "http://10.10.40.*",
                "http://10.10.41.*",
                "https://10.10.40.*",
                "https://10.10.41.*",// Local network access
                "https://tvboot.com",
                "https://*.tvboot.com"
        ));

       //ONLY DEV  HG32EJ690  T-KTM2DEUCB-1360.1
        configuration.addAllowedOrigin("file://");


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
                            .requestMatchers(ADMIN_ENDPOINTS).hasRole(Role.ADMIN.name())

                            // Manager and above endpoints
                            .requestMatchers(MANAGER_ENDPOINTS).hasAnyRole(Role.ADMIN.name(), Role.MANAGER.name())

                            // Terminal-specific endpoints
                            .requestMatchers("/api/v1/terminal/**").hasRole("TERMINAL")
                            .requestMatchers("/api/v1/stream/**").hasRole("TERMINAL")
                            .requestMatchers("/api/v1/epg/**").hasRole("TERMINAL")

                            // Channel management - Manager and above can create/modify, others can view
                            .requestMatchers("GET", "/api/v1/tvchannels/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN", "RECEPTIONIST")
                            .requestMatchers("POST", "/api/v1/tvchannels/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")
                            .requestMatchers("PUT", "/api/v1/tvchannels/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")
                            .requestMatchers("DELETE", "/api/v1/tvchannels/**").hasAnyRole("ADMIN", "MANAGER")

                            // Terminal management - Technician and above
                            .requestMatchers("/api/v1/terminals/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")

                            // Room management - Receptionist and above
                            .requestMatchers("/api/v1/rooms/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Guest management - Receptionist and above
                            .requestMatchers("/api/v1/guests/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Reservation management - Receptionist and above
//                            .requestMatchers("/api/v1/reservations/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Language management - Admin can modify, others can view
                            .requestMatchers("GET", "/api/v1/languages/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN", "RECEPTIONIST")
                            .requestMatchers("POST", "/api/v1/languages/**").hasRole("ADMIN")
                            .requestMatchers("PUT", "/api/v1/languages/**").hasRole("ADMIN")
                            .requestMatchers("DELETE", "/api/v1/languages/**").hasRole("ADMIN")

                            // Category management - Manager and above
                            .requestMatchers("GET", "/api/v1/categories/**").hasAnyRole(Role.ADMIN.name(), "MANAGER", "TECHNICIAN", "RECEPTIONIST")
                            .requestMatchers("POST", "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("PUT", "/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("DELETE", "/api/v1/categories/**").hasRole("ADMIN")

                            // Package management - Manager and above
//                            .requestMatchers("/api/v1/packages/**").hasAnyRole("ADMIN", "MANAGER")

                            // Hotel information - All authenticated users
                            .requestMatchers("/api/v1/hotel/**").authenticated()

                            // Profile management - All authenticated users
                            .requestMatchers(AUTHENTICATED_ENDPOINTS).authenticated()

                            // All other API endpoints require authentication
                            .requestMatchers("/api/v1/**").authenticated()

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
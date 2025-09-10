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
public class ProductionSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionHandler securityExceptionHandler;

    // Public endpoints that don't require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/public/**",
            "/api/test/public",
            "/api/test/health",
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

    // Admin-only endpoints
    private static final String[] ADMIN_ENDPOINTS = {
            "/api/auth/register",
            "/api/users/**",
            "/api/languages/initialize",
            "/api/languages/import",
            "/api/languages/*/delete",
            "/api/test/admin"
    };

    // Manager and above endpoints
    private static final String[] MANAGER_ENDPOINTS = {
            "/api/languages/**",
            "/api/categories/**",
            "/api/packages/**"
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
                "http://192.168.*:*",
                "https://tvboot.com",
                "https://*.tvboot.com"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "X-Total-Count"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Production Security Filter Chain...");

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> {
                    log.info("Configuring authorization rules...");
                    authz
                            // Public endpoints
                            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                            // Admin-only endpoints
                            .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")

                            // Manager and above endpoints
                            .requestMatchers(MANAGER_ENDPOINTS).hasAnyRole("ADMIN", "MANAGER")

                            // Channel management - Manager and above
                            .requestMatchers("/api/channels/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")

                            // Terminal management - Technician and above
                            .requestMatchers("/api/terminals/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")

                            // Room management - Receptionist and above
                            .requestMatchers("/api/rooms/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Guest management - Receptionist and above
                            .requestMatchers("/api/guests/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Reservation management - Receptionist and above
                            .requestMatchers("/api/reservations/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                            // Hotel information - All authenticated users
                            .requestMatchers("/api/hotel/**").authenticated()

                            // Profile management
                            .requestMatchers("/api/auth/me").authenticated()
                            .requestMatchers("/api/test/protected").authenticated()

                            // All other API endpoints require authentication
                            .requestMatchers("/api/**").authenticated()

                            // Deny all other requests
                            .anyRequest().denyAll();
                })
                .exceptionHandling(exceptions -> {
                    log.info("Configuring exception handling");
                    exceptions
                            .authenticationEntryPoint(securityExceptionHandler)
                            .accessDeniedHandler(securityExceptionHandler);
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
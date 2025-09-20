//package com.tvboot.tivio.config;
//
//import com.tvboot.tivio.common.security.JwtAuthenticationFilter;
//import com.tvboot.tivio.auth.UserDetailsServiceImpl;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.List;
//
//@Slf4j
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//@RequiredArgsConstructor
//public class SecurityConfig02 {
//
//    private final UserDetailsServiceImpl userDetailsService;
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final SecurityExceptionHandler securityExceptionHandler;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        authProvider.setHideUserNotFoundExceptions(false);
//        return authProvider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        log.info("Configuring CORS...");
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Allow specific origins
//        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
//
//        // Allow all methods
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//
//        // Allow all headers
//        configuration.setAllowedHeaders(List.of("*"));
//
//        // Allow credentials
//        configuration.setAllowCredentials(true);
//
//        // Expose headers
//        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        log.info("Configuring Security Filter Chain...");
//
//        return http
//                .cors(cors -> {
//                    log.info("Applying CORS configuration");
//                    cors.configurationSource(corsConfigurationSource());
//                })
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> {
//                    log.info("Setting session management to stateless");
//                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//                })
//                .authorizeHttpRequests(authz -> {
//                    log.info("Configuring authorization rules...");
//                    authz
//                            // Public endpoints - Order matters!
//                            .requestMatchers("/api/auth/login").permitAll()
//                            .requestMatchers("/api/auth/me").authenticated()
//                            .requestMatchers("/api/auth/register").hasRole("ADMIN")
//                            .requestMatchers("/api/public/**").permitAll()
//                            .requestMatchers("/api/test/public").permitAll()
//                            .requestMatchers("/h2-console/**").permitAll()
//                            .requestMatchers("/error").permitAll()
//
//                            // Test endpoints
//                            .requestMatchers("/api/test/admin").hasRole("ADMIN")
//                            .requestMatchers("/api/test/protected").authenticated()
//
//                            // All other API endpoints require authentication
//                            .requestMatchers("/api/**").authenticated()
//
//                            // Any other request
//                            .anyRequest().authenticated();
//                })
//                .exceptionHandling(exceptions -> {
//                    log.info("Configuring exception handling");
//                    exceptions
//                            .authenticationEntryPoint(securityExceptionHandler)
//                            .accessDeniedHandler(securityExceptionHandler);
//                })
//                .authenticationProvider(authenticationProvider())
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .build();
//    }
//}
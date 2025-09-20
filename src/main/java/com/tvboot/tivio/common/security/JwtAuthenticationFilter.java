package com.tvboot.tivio.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    // Define public endpoints that should skip JWT processing
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/login",
            "/api/public/",
            "/api/test/public",
            "/api/test/health",
            "api/test/info",
            "/h2-console/",
            "/error",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/swagger-resources/"

    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldSkip = PUBLIC_ENDPOINTS.stream()
                .anyMatch(publicPath -> path.startsWith(publicPath));

        if (shouldSkip) {
            log.debug("Skipping JWT filter for public endpoint: {}", path);
        } else {
            log.debug("Processing JWT filter for endpoint: {}", path);
        }

        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("JWT Filter processing request to: {}", path);

        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                log.debug("JWT token found in request to: {}", path);

                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    log.debug("Valid JWT found for user: {} accessing: {}", username, path);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Double validation with user details
                    if (jwtUtils.validateJwtToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authentication set for user: {} with authorities: {}",
                                username, userDetails.getAuthorities());
                    } else {
                        log.warn("JWT token validation with user details failed for user: {}", username);
                    }
                } else {
                    log.warn("Invalid JWT token for request to: {}", path);
                }
            } else {
                log.debug("No JWT token found in request to: {}", path);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication for path {}: {}", path, e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
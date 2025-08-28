package com.tvboot.tivio.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("=== Health endpoint called at {} ===", LocalDateTime.now());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Health check successful - no authentication required");
        response.put("application", "TVBOOT IPTV Platform");
        response.put("version", "1.0.0");

        log.info("=== Health endpoint response: {} ===", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        log.info("=== Public endpoint called at {} ===", LocalDateTime.now());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("authentication", "Not required");
        response.put("access", "PUBLIC");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        log.info("=== Protected endpoint called at {} ===", LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello " + auth.getName() + "! This is a protected endpoint");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("access", "AUTHENTICATED");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminEndpoint() {
        log.info("=== Admin endpoint called at {} ===", LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access granted for " + auth.getName());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities());
        response.put("access", "ADMIN_ONLY");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        log.info("=== Info endpoint called ===");

        Map<String, Object> response = new HashMap<>();
        response.put("application", "TVBOOT IPTV Platform");
        response.put("description", "Hotel IPTV Management System");
        response.put("endpoints", Map.of(
                "/api/test/health", "Health check - Public",
                "/api/test/public", "Public endpoint - No auth",
                "/api/test/protected", "Protected endpoint - Auth required",
                "/api/test/admin", "Admin endpoint - Admin role required",
                "/api/auth/login", "Login endpoint - Public",
                "/api/test/debug-users", "Debug users - Public (temp)"
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug-users")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        log.info("=== Debug Users endpoint called ===");

        // This is a temporary debug endpoint - remove in production
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Database connection test");
            response.put("timestamp", LocalDateTime.now().toString());

            // You can add user count check here if needed
            response.put("note", "Check application logs for user initialization details");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Database connection failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
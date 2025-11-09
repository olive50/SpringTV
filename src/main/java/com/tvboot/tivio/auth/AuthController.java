package com.tvboot.tivio.auth;

import com.tvboot.tivio.auth.dto.JwtResponse;
import com.tvboot.tivio.auth.dto.LoginRequest;
import com.tvboot.tivio.auth.dto.MessageResponse;
import com.tvboot.tivio.auth.dto.SignupRequest;

// Imports Swagger V3 complets
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Imports Spring
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentification", description = "Gestion de l'authentification et des sessions utilisateurs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Parameter(description = "Identifiants de l'utilisateur", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Tentative d'authentification pour l'utilisateur: {}", loginRequest.getUsername());
            JwtResponse response = authService.authenticateUser(loginRequest);
            log.info("Authentification réussie pour: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Échec d'authentification pour {}: identifiants invalides", loginRequest.getUsername());
            // Message spécifique pour les identifiants invalides
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid username or password"));

        } catch (DisabledException e) {
            log.warn("Compte désactivé pour: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Account is disabled"));

        } catch (Exception e) {
            log.error("Erreur d'authentification pour {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            // Le message générique reste ici pour les autres types d'erreurs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Authentication failed. Please try again later."));
        }
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User currentUser = authService.getCurrentUser();
            return ResponseEntity.ok(currentUser);
        } catch (RuntimeException e) {
            log.warn("Impossible de récupérer l'utilisateur actuel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid or expired token"));
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> registerUser(
            @Valid @RequestBody SignupRequest signUpRequest) {
        try {
            MessageResponse response = authService.registerUser(signUpRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création de l'utilisateur: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
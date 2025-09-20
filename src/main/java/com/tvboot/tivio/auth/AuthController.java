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
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentification", description = "Gestion de l'authentification et des sessions utilisateurs")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "🚀 Connexion utilisateur",
            description = """
                    Authentifie un utilisateur avec ses identifiants et retourne un token JWT.
                    
                    **Comptes de test disponibles :**
                    - Admin : admin / admin123
                    - Manager : manager / admin123
                    - Réceptionniste : receptionist / admin123
                    - Technicien : technician / admin123
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Identifiants de connexion (username et password)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "👤 Connexion Admin",
                                            description = "Exemple de connexion administrateur",
                                            value = """
                                                    {
                                                      "username": "admin",
                                                      "password": "admin123"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "🏨 Connexion Manager",
                                            description = "Exemple de connexion gestionnaire",
                                            value = """
                                                    {
                                                      "username": "manager", 
                                                      "password": "admin123"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "🔧 Connexion Technicien",
                                            description = "Exemple de connexion technicien",
                                            value = """
                                                    {
                                                      "username": "technician",
                                                      "password": "admin123"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Connexion réussie - Token JWT généré",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class),
                            examples = @ExampleObject(
                                    name = "Réponse de connexion réussie",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "type": "Bearer",
                                              "username": "admin",
                                              "email": "admin@tvboot.com",
                                              "firstName": "System",
                                              "lastName": "Administrator",
                                              "role": "ADMIN",
                                              "isActive": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "❌ Identifiants invalides ou compte désactivé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Identifiants incorrects",
                                            value = """
                                                    {
                                                      "message": "Invalid username or password"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Compte désactivé",
                                            value = """
                                                    {
                                                      "message": "Account is disabled"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "📋 Données de requête invalides",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "🔧 Erreur interne du serveur",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid username or password"));

        } catch (DisabledException e) {
            log.warn("Compte désactivé pour: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Account is disabled"));

        } catch (Exception e) {
            log.error("Erreur d'authentification pour {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Authentication failed. Please try again later."));
        }
    }

    @Operation(
            summary = "👤 Profil utilisateur",
            description = "Récupère les informations de l'utilisateur actuellement connecté",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Profil utilisateur récupéré",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "🔒 Token invalide ou expiré",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
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

    @Operation(
            summary = "➕ Créer un utilisateur",
            description = "Crée un nouvel utilisateur dans le système (Administrateur uniquement)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Utilisateur créé avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "📋 Données invalides",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "🚫 Accès refusé - Admin requis",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "⚠️ Utilisateur déjà existant",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
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
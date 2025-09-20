package com.tvboot.tivio.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TVBOOT IPTV Platform API",
                version = "1.2.0",
                description = """
                        # TVBOOT IPTV Platform API
                        
                        Système de gestion IPTV complet pour hôtels développé avec Spring Boot.
                        
                        ## Fonctionnalités principales :
                        - **Gestion des chaînes TV** : CRUD complet, catégories, langues
                        - **Gestion hôtelière** : Chambres, clients, réservations
                        - **Terminaux IPTV** : Devices, monitoring, heartbeat
                        - **Authentification JWT** : Sécurité basée sur les rôles
                        - **API RESTful** : Standards REST avec pagination
                        
                        ## Architecture :
                        - Backend : Spring Boot 3.5.0 + Java 21
                        - Database : MySQL 8.0+
                        - Security : JWT + Spring Security
                        - Frontend : Samsung Tizen, LG WebOS, Angular
                        
                        ## Authentification :
                        1. Utilisez `/api/auth/login` pour obtenir un token JWT
                        2. Ajoutez le token dans le header : `Authorization: Bearer <token>`
                        3. Testez avec les utilisateurs par défaut (mot de passe: admin123)
                        """,
                contact = @Contact(
                        name = "TVBOOT Team",
                        email = "support@tvboot.com",
                        url = "https://tvboot.com"
                ),
                license = @License(
                        name = "Proprietary License",
                        url = "https://tvboot.com/license"
                )
        ),
        servers = {
                @Server(
                        description = "Environnement de développement",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Environnement de production",
                        url = "https://api.tvboot.com"
                )
        },
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Entrez le token JWT obtenu depuis l'endpoint /api/auth/login"
)
public class OpenApiConfig {

    @Value("${app.version:1.2.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("TVBOOT IPTV Platform API")
                        .version(appVersion)
                        .description("API complète pour la gestion IPTV hôtelière"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT pour l'authentification")));
    }
}
package com.tvboot.tivio.common.util;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v0/files")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "📁 File Server", description = "Serveur de fichiers générique pour images et documents")
public class FileController1 {

    @Value("${app.file.base-dir:uploads}")
    private String baseDirectory;

    // Types de fichiers autorisés pour la sécurité
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico"
    );

    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "txt", "csv"
    );

    @Operation(
            summary = "🖼️ Récupérer une image",
            description = """
                    Récupère une image par son chemin relatif.
                    
                    **Exemples d'utilisation :**
                    - `/api/files/image/logos/channel-logo.png`
                    - `/api/files/image/avatars/user-avatar.jpg`
                    - `/api/files/image/backgrounds/hotel-bg.webp`
                    
                    **Sécurité :** Seules les extensions d'image autorisées sont servies.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Image trouvée et retournée"),
            @ApiResponse(responseCode = "404", description = "❌ Image non trouvée"),
            @ApiResponse(responseCode = "403", description = "🚫 Type de fichier non autorisé"),
            @ApiResponse(responseCode = "400", description = "📋 Chemin invalide")
    })
    @GetMapping("/image/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        return serveFile(request, ALLOWED_IMAGE_EXTENSIONS, "image");
    }

    @Operation(
            summary = "📄 Récupérer un document",
            description = """
                    Récupère un document par son chemin relatif.
                    
                    **Exemples d'utilisation :**
                    - `/api/files/document/reports/monthly-report.pdf`
                    - `/api/files/document/manuals/user-guide.pdf`
                    
                    **Sécurité :** Seules les extensions de document autorisées sont servies.
                    """
    )
    @GetMapping("/document/**")
    public ResponseEntity<Resource> getDocument(HttpServletRequest request) {
        return serveFile(request, ALLOWED_DOCUMENT_EXTENSIONS, "document");
    }

    private ResponseEntity<Resource> serveFile(HttpServletRequest request,
                                               List<String> allowedExtensions,
                                               String type) {
        try {
            // Extraire le chemin relatif depuis l'URL
            String requestPath = request.getRequestURI();
            String relativePath = extractRelativePath(requestPath, type);

            if (!StringUtils.hasText(relativePath)) {
                log.warn("Chemin vide pour le type: {}", type);
                return ResponseEntity.badRequest().build();
            }

            // Validation de sécurité
            if (!isValidPath(relativePath) || !hasAllowedExtension(relativePath, allowedExtensions)) {
                log.warn("Tentative d'accès à un fichier non autorisé: {}", relativePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Construire le chemin complet et sécurisé
            Path filePath = Paths.get(baseDirectory)
                    .resolve(relativePath)
                    .normalize();

            // Vérifier que le fichier reste dans le répertoire autorisé (protection contre path traversal)
            if (!filePath.startsWith(Paths.get(baseDirectory).normalize())) {
                log.warn("Tentative de path traversal détectée: {}", relativePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(filePath);
                String filename = filePath.getFileName().toString();

                log.debug("Fichier {} servi avec succès (type: {})", filename, contentType);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600") // Cache 1h
                        .body(resource);
            } else {
                log.info("Fichier non trouvé ou non lisible: {}", relativePath);
                return ResponseEntity.notFound().build();
            }

        } catch (MalformedURLException e) {
            log.error("URL malformée: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du fichier: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String extractRelativePath(String requestPath, String type) {
        String prefix = "/files/" + type + "/";
        if (requestPath.startsWith(prefix)) {
            return requestPath.substring(prefix.length());
        }
        return "";
    }

    private boolean isValidPath(String path) {
        // Vérifications de sécurité basiques
        return !path.contains("..") &&
                !path.startsWith("/") &&
                !path.contains("\\") &&
                StringUtils.hasText(path);
    }

    private boolean hasAllowedExtension(String filename, List<String> allowedExtensions) {
        String extension = getFileExtension(filename);
        return allowedExtensions.contains(extension.toLowerCase());
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    private String determineContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType != null) {
                return contentType;
            }
        } catch (IOException e) {
            log.debug("Impossible de déterminer le type de contenu pour: {}", filePath);
        }

        // Fallback basé sur l'extension
        String extension = getFileExtension(filePath.getFileName().toString()).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "ico" -> "image/x-icon";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "csv" -> "text/csv";
            default -> "application/octet-stream";
        };
    }
}
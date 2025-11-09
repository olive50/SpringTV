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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/files")
@CrossOrigin(origins = {"http://localhost:4200", "${app.cors.allowed-origins:*}"})
@Tag(name = "📁 File Server", description = "Serveur de fichiers pour plateforme IPTV")
public class FileController {

    @Value("${app.file.base-dir:uploads}")
    private String baseDirectory;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Types de fichiers autorisés pour la sécurité
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico"
    );

    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = Arrays.asList(
            "mp4", "m3u8", "ts", "mkv", "avi", "mov", "webm"
    );

    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "txt", "csv", "m3u", "xml", "json"
    );

    @Operation(
            summary = "🖼️ Récupérer une image",
            description = """
                    Récupère une image par son chemin relatif.
                                        
                    **Exemples d'utilisation :**
                    - `/api/v1/files/image/logos/channel-logo.png`
                    - `/api/v1/files/image/elwatania-dz.gif`
                                        
                    **Sécurité :** Seules les extensions d'image autorisées sont servies.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Image trouvée et retournée"),
            @ApiResponse(responseCode = "404", description = "❌ Image non trouvée"),
            @ApiResponse(responseCode = "403", description = "🚫 Type de fichier non autorisé"),
            @ApiResponse(responseCode = "400", description = "📋 Chemin invalide")
    })
    @GetMapping(value = "/image/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        String path = extractPathFromPattern(request);
        return serveFile(path, ALLOWED_IMAGE_EXTENSIONS, "image");
    }

    @Operation(
            summary = "📺 Récupérer une vidéo ou stream",
            description = """
                    Récupère une vidéo ou un flux IPTV.
                                        
                    **Exemples d'utilisation :**
                    - `/api/v1/files/video/streams/channel.m3u8`
                    - `/api/v1/files/video/vod/movie.mp4`
                    """
    )
    @GetMapping(value = "/video/**")
    public ResponseEntity<Resource> getVideo(HttpServletRequest request) {
        String path = extractPathFromPattern(request);
        return serveFile(path, ALLOWED_VIDEO_EXTENSIONS, "video");
    }

    @Operation(
            summary = "📄 Récupérer un document",
            description = """
                    Récupère un document ou playlist.
                                        
                    **Exemples d'utilisation :**
                    - `/api/v1/files/document/playlists/channels.m3u`
                    - `/api/v1/files/document/epg/guide.xml`
                    """
    )
    @GetMapping(value = "/document/**")
    public ResponseEntity<Resource> getDocument(HttpServletRequest request) {
        String path = extractPathFromPattern(request);
        return serveFile(path, ALLOWED_DOCUMENT_EXTENSIONS, "document");
    }

    /**
     * Extrait le chemin relatif après le pattern de mapping
     * Utilise HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE pour obtenir le bon chemin
     */
    private String extractPathFromPattern(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        log.debug("Full path: {}", path);
        log.debug("Best match pattern: {}", bestMatchPattern);

        // Utiliser AntPathMatcher pour extraire la partie variable
        String pattern = bestMatchPattern;
        String extractedPath = pathMatcher.extractPathWithinPattern(pattern, path);

        log.debug("Extracted path: {}", extractedPath);
        return extractedPath;
    }

    private ResponseEntity<Resource> serveFile(String relativePath,
                                               List<String> allowedExtensions,
                                               String type) {
        try {
            log.debug("Serving {} file: {}", type, relativePath);

            // Validation basique
            if (!StringUtils.hasText(relativePath)) {
                log.warn("Chemin vide pour le type: {}", type);
                return ResponseEntity.badRequest()
                        .header("X-Error", "Empty path")
                        .build();
            }

            // Validation de sécurité
            if (!isValidPath(relativePath)) {
                log.warn("Chemin invalide détecté: {}", relativePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error", "Invalid path")
                        .build();
            }

            if (!hasAllowedExtension(relativePath, allowedExtensions)) {
                log.warn("Extension non autorisée pour: {}", relativePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error", "Forbidden file extension")
                        .build();
            }

            // Construire le chemin complet
            Path basePath = Paths.get(baseDirectory).toAbsolutePath().normalize();

            // Créer le répertoire de base s'il n'existe pas
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("Répertoire de base créé: {}", basePath);
            }

            Path filePath = basePath.resolve(relativePath).normalize();

            // Protection contre path traversal
            if (!filePath.startsWith(basePath)) {
                log.warn("Tentative de path traversal détectée: {}", relativePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error", "Path traversal detected")
                        .build();
            }

            // Vérifier l'existence du fichier
            if (!Files.exists(filePath)) {
                log.info("Fichier non trouvé: {} (chemin complet: {})", relativePath, filePath);
                return ResponseEntity.notFound()
                        .header("X-Error", "File not found: " + relativePath)
                        .build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.isReadable()) {
                log.warn("Fichier non lisible: {}", filePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error", "File not readable")
                        .build();
            }

            // Déterminer le type de contenu
            String contentType = determineContentType(filePath);
            String filename = filePath.getFileName().toString();
            long fileSize = Files.size(filePath);

            log.info("✅ Serving {} '{}' ({} bytes, type: {})", type, filename, fileSize, contentType);

            // Construire la réponse avec les bons headers
            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));

            // Headers spécifiques selon le type
            if (type.equals("video") || isStreamingContent(filename)) {
                // Headers pour le streaming vidéo
                responseBuilder
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .header("X-Content-Type-Options", "nosniff");
            } else if (type.equals("image")) {
                // Cache plus long pour les images
                responseBuilder.header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400"); // 24h
            } else {
                // Cache normal pour les documents
                responseBuilder.header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600"); // 1h
            }

            return responseBuilder.body(resource);

        } catch (MalformedURLException e) {
            log.error("URL malformée: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Error", "Malformed URL")
                    .build();
        } catch (IOException e) {
            log.error("Erreur IO: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error", "IO Error")
                    .build();
        } catch (Exception e) {
            log.error("Erreur inattendue: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error", "Internal Server Error")
                    .build();
        }
    }

    private boolean isValidPath(String path) {
        return !path.contains("..") &&
                !path.contains("~") &&
                !path.contains("\\") &&
                !path.contains("//") &&
                !path.startsWith("/") &&
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

    private boolean isStreamingContent(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals("m3u8") || extension.equals("ts") || extension.equals("m3u");
    }

    private String determineContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType != null) {
                return contentType;
            }
        } catch (IOException e) {
            log.debug("Impossible de déterminer le type MIME pour: {}", filePath);
        }

        // Fallback basé sur l'extension
        String extension = getFileExtension(filePath.getFileName().toString()).toLowerCase();
        return switch (extension) {
            // Images
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "ico" -> "image/x-icon";

            // Vidéos et IPTV
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "m3u8" -> "application/vnd.apple.mpegurl";
            case "ts" -> "video/mp2t";
            case "mkv" -> "video/x-matroska";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "m3u" -> "audio/x-mpegurl";

            // Documents
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain; charset=UTF-8";
            case "csv" -> "text/csv; charset=UTF-8";
            case "xml" -> "application/xml";
            case "json" -> "application/json";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            default -> "application/octet-stream";
        };
    }
}
package com.tvboot.tivio.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvboot.tivio.audit.Auditable;
import com.tvboot.tivio.dto.*;
import com.tvboot.tivio.service.TvChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Chaînes TV", description = "Gestion des chaînes de télévision")
@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class TvChannelController {

    private final TvChannelService tvChannelService;
    @Autowired
    private Validator validator;

    @Operation(
            summary = "Liste des chaînes",
            description = "Récupère toutes les chaînes TV avec leurs catégories et langues"
    )
    @GetMapping
    @Auditable(action = "LIST_CHANNELS", resource = "TV_CHANNEL")
    public ResponseEntity<List<TvChannelDTO>> getAllChannels() {
        log.debug("Received request to get all TV channels");
        List<TvChannelDTO> channels = tvChannelService.getAllChannels();
        log.debug("Returning {} TV channels", channels.size());
        return ResponseEntity.ok(channels);
    }

    @Operation(
            summary = "Chaînes paginées",
            description = "Récupère les chaînes TV avec pagination et tri"
    )
    @GetMapping("/paged")
    public ResponseEntity<Page<TvChannelDTO>> getAllChannelsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "channelNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TvChannelDTO> channels = tvChannelService.getAllChannels(pageable);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TvChannelDTO> getChannelById(@PathVariable Long id) {
        return tvChannelService.getChannelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{channelNumber}")
    public ResponseEntity<TvChannelDTO> getChannelByNumber(@PathVariable int channelNumber) {
        return tvChannelService.getChannelByNumber(channelNumber)
                .map(channel -> ResponseEntity.ok(channel))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TvChannelDTO>> getChannelsByCategory(@PathVariable Long categoryId) {
        List<TvChannelDTO> channels = tvChannelService.getChannelsByCategory(categoryId);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/language/{languageId}")
    public ResponseEntity<List<TvChannelDTO>> getChannelsByLanguage(@PathVariable Long languageId) {
        List<TvChannelDTO> channels = tvChannelService.getChannelsByLanguage(languageId);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TvChannelDTO>> searchChannels(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TvChannelDTO> channels = tvChannelService.searchChannelsByName(name, pageable);
        return ResponseEntity.ok(channels);
    }

    @Operation(
            summary = "Créer une chaîne",
            description = "Crée une nouvelle chaîne TV"
    )
    @PostMapping
    @Auditable(action = "CREATE_CHANNEL", resource = "TV_CHANNEL", logParams = true, logResult = true)
    public ResponseEntity<TvChannelDTO> createChannel(@Valid @RequestBody TvChannelCreateDTO createDTO) {
        log.info("Received request to create TV channel: {}", createDTO.getName());

        try {
            TvChannelDTO createdChannel = tvChannelService.createChannel(createDTO);
            log.info("Successfully created TV channel with ID: {}", createdChannel.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);

        } catch (Exception e) {
            log.error("Failed to create TV channel: {}", createDTO.getName(), e);
            throw e;
        }
    }

    @Operation(
            summary = "Créer une chaîne avec un logo",
            description = "Crée une nouvelle chaîne TV et télécharge son logo en même temps"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chaîne créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "413", description = "Fichier trop volumineux"),
            @ApiResponse(responseCode = "415", description = "Type de fichier non supporté"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @PostMapping(path = "/with-logo", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Auditable(action = "CREATE_CHANNEL_WITH_LOGO", resource = "TV_CHANNEL", logParams = true, logResult = true)
    public ResponseEntity<?> createChannelWithLogo(
            @Valid @RequestPart("channelData") TvChannelCreateDTO createDTO,
            @RequestPart("logoFile") MultipartFile logoFile) {

        log.info("Received request to create TV channel with logo: {}", createDTO.getName());

        try {
            // Validation du fichier logo
            if (logoFile.isEmpty()) {
                log.warn("Logo file is empty for channel: {}", createDTO.getName());
                return ResponseEntity.badRequest().body(createErrorResponse("EMPTY_LOGO_FILE",
                        "Logo file is empty"));
            }

            // Créer la chaîne avec le logo
            TvChannelDTO createdChannel = tvChannelService.createChannelWithLogo(createDTO, logoFile);
            log.info("Successfully created TV channel with logo - ID: {}, Logo: {}",
                    createdChannel.getId(), createdChannel.getLogoUrl());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);

        } catch (IOException e) {
            log.error("I/O error creating channel with logo: {}", createDTO.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("LOGO_UPLOAD_IO_ERROR",
                            "Failed to upload logo due to I/O error: " + e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.error("Invalid file type for channel logo: {}", createDTO.getName(), e);
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(createErrorResponse("INVALID_FILE_TYPE",
                            "Invalid file type. Only image files are supported."));

        } catch (Exception e) {
            log.error("Unexpected error creating channel with logo: {}", createDTO.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("CHANNEL_CREATION_ERROR",
                            "Failed to create channel: " + e.getMessage()));
        }
    }

    @Operation(summary = "Modifier une chaîne")
    @PutMapping("/{id}")
    @Auditable(action = "UPDATE_CHANNEL", resource = "TV_CHANNEL", logParams = true)
    public ResponseEntity<TvChannelDTO> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody TvChannelUpdateDTO updateDTO) {

        log.info("Received request to update TV channel with ID: {}", id);

        try {
            TvChannelDTO updatedChannel = tvChannelService.updateChannel(id, updateDTO);
            log.info("Successfully updated TV channel: {}", updatedChannel.getName());
            return ResponseEntity.ok(updatedChannel);

        } catch (Exception e) {
            log.error("Failed to update TV channel with ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/create-with-logo")
    public ResponseEntity<?> createChannelWithLogoForm(
            @RequestParam("name") String name,
            @RequestParam("number") Integer number,
            @RequestParam("port") Integer port,
            @RequestParam("categoryId") Long category,
            @RequestParam("languageId") Long language,
            @RequestParam("ip") String streamUrl,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            @RequestParam(value = "quality", required = false) String quality,

            @RequestPart("logoFile") MultipartFile logoFile) {

        log.info("Received request to create TV channel with logo: {}", name);
        log.info("Channel parameters - Number: {}, Category: {}, StreamUrl: {}", number, category, streamUrl);
        log.info("Logo file: {}, Size: {} bytes", logoFile.getOriginalFilename(), logoFile.getSize());

        try {
            // Validation des paramètres obligatoires
            if (name == null || name.trim().isEmpty()) {
                log.error("Channel name is null or empty");
                return ResponseEntity.badRequest().body(createErrorResponse("VALIDATION_ERROR",
                        "Channel name is required"));
            }

            if (number == null) {
                log.error("Channel number is null");
                return ResponseEntity.badRequest().body(createErrorResponse("VALIDATION_ERROR",
                        "Channel number is required"));
            }



//            if (streamUrl == null || streamUrl.trim().isEmpty()) {
//                log.error("Stream URL is null or empty");
//                return ResponseEntity.badRequest().body(createErrorResponse("VALIDATION_ERROR",
//                        "Stream URL is required"));
//            }

            // Validation du numéro de chaîne
            if (number <= 0) {
                log.error("Invalid channel number: {}", number);
                return ResponseEntity.badRequest().body(createErrorResponse("VALIDATION_ERROR",
                        "Channel number must be positive"));
            }

//            // Validation de l'URL de stream (format basique)
//            if (!streamUrl.startsWith("http://") && !streamUrl.startsWith("https://")) {
//                log.error("Invalid stream URL format: {}", streamUrl);
//                return ResponseEntity.badRequest().body(createErrorResponse("VALIDATION_ERROR",
//                        "Stream URL must start with http:// or https://"));
//            }
//
//            // Validation de la catégorie (optionnel: liste prédéfinie)
//            Set<String> validCategories = Set.of("GENERAL", "SPORT", "NEWS", "ENTERTAINMENT",
//                    "DOCUMENTARY", "CHILDREN", "MUSIC", "MOVIES");
//            if (!validCategories.contains(category)) {
//                log.warn("Unknown category: {}. Using as-is.", category);
//            }

            // Validation du fichier logo
            if (logoFile.isEmpty()) {
                log.warn("Logo file is empty for channel: {}", name);
                return ResponseEntity.badRequest().body(createErrorResponse("EMPTY_LOGO_FILE",
                        "Logo file is empty"));
            }

            // Validation du type de fichier
            String contentType = logoFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid file type for logo: {} (content-type: {})",
                        logoFile.getOriginalFilename(), contentType);
                return ResponseEntity.badRequest().body(createErrorResponse("INVALID_FILE_TYPE",
                        "Invalid file type. Only image files are supported."));
            }

            // Validation de la taille du fichier (max 5MB)
            long maxFileSize = 5 * 1024 * 1024; // 5MB
            if (logoFile.getSize() > maxFileSize) {
                log.warn("Logo file too large: {} bytes (max: {} bytes)",
                        logoFile.getSize(), maxFileSize);
                return ResponseEntity.badRequest().body(createErrorResponse("FILE_TOO_LARGE",
                        "Logo file is too large. Maximum size is 5MB."));
            }

            // Créer le DTO à partir des paramètres
            TvChannelCreateDTO createDTO = new TvChannelCreateDTO();
            createDTO.setName(name.trim());
            createDTO.setChannelNumber(number);
            createDTO.setCategoryId(category);
            createDTO.setIp(streamUrl.trim());
            createDTO.setDescription(description != null ? description.trim() : null);

            createDTO.setLanguageId(language);


            log.info("Created DTO for channel: {}", createDTO.getName());

            // Validation supplémentaire avec le validator (si vous avez des annotations @Valid dans le DTO)
            Set<ConstraintViolation<TvChannelCreateDTO>> violations = validator.validate(createDTO);
            if (!violations.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Validation errors: ");
                for (ConstraintViolation<TvChannelCreateDTO> violation : violations) {
                    errorMessage.append(violation.getPropertyPath())
                            .append(" ")
                            .append(violation.getMessage())
                            .append("; ");
                }
                log.error("DTO validation failed: {}", errorMessage.toString());
                return ResponseEntity.badRequest().body(createErrorResponse("VALIDATION_ERROR",
                        errorMessage.toString()));
            }

            // Créer la chaîne avec le logo
            TvChannelDTO createdChannel = tvChannelService.createChannelWithLogo(createDTO, logoFile);
            log.info("Successfully created TV channel with logo - ID: {}, Name: {}, Logo: {}",
                    createdChannel.getId(), createdChannel.getName(), createdChannel.getLogoUrl());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);

        } catch (IOException e) {
            log.error("I/O error creating channel with logo: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("LOGO_UPLOAD_IO_ERROR",
                            "Failed to upload logo due to I/O error: " + e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.error("Invalid argument for channel creation: {}", name, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("INVALID_ARGUMENT",
                            "Invalid argument: " + e.getMessage()));

        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation during channel creation: {}", name, e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("DUPLICATE_CHANNEL",
                            "Channel with this name or number already exists"));

        } catch (Exception e) {
            log.error("Unexpected error creating channel with logo: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("CHANNEL_CREATION_ERROR",
                            "Failed to create channel: " + e.getMessage()));
        }
    }






    // Ou si vous préférez injecter directement le ValidatorFactory
/*
private final Validator validator;

@Autowired
public TvChannelController(ValidatorFactory validatorFactory) {
    this.validator = validatorFactory.getValidator();
}
*/
    @Operation(summary = "Supprimer une chaîne")
    @DeleteMapping("/{id}")
    @Auditable(action = "DELETE_CHANNEL", resource = "TV_CHANNEL")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long id) {
        log.info("Received request to delete TV channel with ID: {}", id);

        try {
            tvChannelService.deleteChannel(id);
            log.info("Successfully deleted TV channel with ID: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Failed to delete TV channel with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Méthode utilitaire pour créer une réponse d'erreur standardisée
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}
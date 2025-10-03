package com.tvboot.tivio.language;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
import com.tvboot.tivio.language.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*")
public class LanguageController {

    private final LanguageService languageService;
    private final LanguageMapper languageMapper;

    @GetMapping
    public ResponseEntity<TvBootHttpResponse> getLanguages(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting languages - page: {}, size: {}", page, size);

        try {
            Page<Language> languagePage = languageService.getLanguages(page, size);
            List<LanguageResponseDTO> languageDTOs = languagePage.getContent().stream()
                    .map(languageMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Languages retrieved successfully")
                    .build()
                    .addData("languages", languageDTOs)
                    .addPagination(page, size, languagePage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving languages", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving languages", e.getMessage());
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<TvBootHttpResponse> getAdminLanguages(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        try {
            Page<Language> languagePage = languageService.getAdminEnabledLanguages(page, size);
            List<LanguageResponseDTO> languageDTOs = languagePage.getContent().stream()
                    .map(languageMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Admin languages retrieved successfully")
                    .build()
                    .addData("languages", languageDTOs)
                    .addPagination(page, size, languagePage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving admin languages", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving admin languages", e.getMessage());
        }
    }

    @GetMapping("/guest")
    public ResponseEntity<TvBootHttpResponse> getGuestLanguages(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        try {
            Page<Language> languagePage = languageService.getGuestEnabledLanguages(page, size);
            List<LanguageResponseDTO> languageDTOs = languagePage.getContent().stream()
                    .map(languageMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guest languages retrieved successfully")
                    .build()
                    .addData("languages", languageDTOs)
                    .addPagination(page, size, languagePage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving guest languages", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving guest languages", e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<TvBootHttpResponse> searchLanguages(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(required = false) Boolean isAdminEnabled,
            @RequestParam(required = false) Boolean isGuestEnabled,
            @RequestParam(required = false) Boolean isRtl) {

        log.info("Searching languages with query: '{}' - page: {}, size: {}", q, page, size);

        String searchQuery = (q != null) ? q.trim() : null;
        if (searchQuery != null && searchQuery.isEmpty()) {
            searchQuery = null;
        }

        try {
            Page<Language> result = languageService.getLanguages(page, size, searchQuery,
                    isAdminEnabled, isGuestEnabled, isRtl);

            List<LanguageResponseDTO> languageDTOs = result.getContent().stream()
                    .map(languageMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Search completed successfully")
                    .build()
                    .addData("languages", languageDTOs)
                    .addPagination(page, size, result.getTotalElements())
                    .addData("searchQuery", q.trim())
                    .addData("resultsFound", result.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error searching languages with query: '{}'", q, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error searching languages", e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> getLanguageById(@PathVariable Long id) {
        log.info("Getting language by id: {}", id);

        try {
            Optional<Language> language = languageService.getLanguageById(id);

            if (language.isPresent()) {
                LanguageResponseDTO languageDTO = languageMapper.toDTO(language.get());

                TvBootHttpResponse response = TvBootHttpResponse.success()
                        .message("Language found")
                        .build()
                        .addData("language", languageDTO);

                return ResponseEntity.ok(response);
            } else {
                return TvBootHttpResponse.notFoundResponse("Language not found with id: " + id);
            }

        } catch (Exception e) {
            log.error("Error retrieving language with id: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving language", e.getMessage());
        }
    }

    @GetMapping("/iso/{iso6391}")
    public ResponseEntity<TvBootHttpResponse> getLanguageByIso(@PathVariable String iso6391) {
        log.info("Getting language by ISO 639-1: {}", iso6391);

        try {
            Optional<Language> language = languageService.getLanguageByIso6391(iso6391);

            if (language.isPresent()) {
                LanguageResponseDTO languageDTO = languageMapper.toDTO(language.get());

                TvBootHttpResponse response = TvBootHttpResponse.success()
                        .message("Language found")
                        .build()
                        .addData("language", languageDTO);

                return ResponseEntity.ok(response);
            } else {
                return TvBootHttpResponse.notFoundResponse("Language not found with ISO 639-1: " + iso6391);
            }

        } catch (Exception e) {
            log.error("Error retrieving language with ISO 639-1: {}", iso6391, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving language", e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<TvBootHttpResponse> createLanguage(@Valid @RequestBody LanguageCreateDTO createDTO) {
        log.info("Creating new language: {}", createDTO.getName());

        try {
            Language createdLanguage = languageService.createLanguage(createDTO);
            LanguageResponseDTO languageDTO = languageMapper.toDTO(createdLanguage);

            TvBootHttpResponse response = TvBootHttpResponse.created()
                    .message("Language created successfully")
                    .build()
                    .addData("language", languageDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating language: {}", e.getMessage());
            return TvBootHttpResponse.badRequestResponse(e.getMessage());

        } catch (Exception e) {
            log.error("Error creating language", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error creating language", e.getMessage());
        }
    }

    @PostMapping(path = "/with-flag", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create language with flag")
    public ResponseEntity<TvBootHttpResponse> createLanguageWithFlag(
            @RequestPart("language") @Valid LanguageCreateDTO createDTO,
            @RequestPart("flag") MultipartFile flagFile) {

        log.info("Creating language with flag: {}", createDTO.getName());

        try {
            LanguageResponseDTO created = languageService.createLanguageWithFlag(createDTO, flagFile);

            TvBootHttpResponse response = TvBootHttpResponse.created()
                    .message("Language created successfully with flag")
                    .build()
                    .addData("language", created);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating language with flag", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error creating language with flag", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update language")
    public ResponseEntity<TvBootHttpResponse> updateLanguage(
            @PathVariable Long id,
            @Valid @RequestBody LanguageUpdateDTO updateDTO) {

        log.info("Updating language with ID: {}", id);

        try {
            LanguageResponseDTO updatedLanguage = languageService.updateLanguage(id, updateDTO);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Language updated successfully")
                    .build()
                    .addData("language", updatedLanguage);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating language with ID: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error updating language", e.getMessage());
        }
    }

    @PutMapping(path = "/{id}/with-flag", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update language with flag")
    public ResponseEntity<TvBootHttpResponse> updateLanguageWithFlag(
            @PathVariable Long id,
            @RequestPart("language") @Valid LanguageUpdateDTO updateDTO,
            @RequestPart(value = "flag", required = false) MultipartFile flagFile) {

        log.info("Updating language with flag - ID: {}", id);

        try {
            LanguageResponseDTO updatedLanguage = languageService.updateLanguageWithFlag(id, updateDTO, flagFile);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Language updated successfully with flag")
                    .build()
                    .addData("language", updatedLanguage);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating language with flag - ID: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error updating language with flag", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> deleteLanguage(@PathVariable Long id) {
        log.info("Deleting language with id: {}", id);

        try {
            languageService.deleteLanguage(id);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Language deleted successfully")
                    .build()
                    .addData("deletedLanguageId", id);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("Language not found for deletion: {}", e.getMessage());
            return TvBootHttpResponse.notFoundResponse(e.getMessage());

        } catch (Exception e) {
            log.error("Error deleting language with id: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error deleting language", e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<TvBootHttpResponse> getLanguageStats() {
        log.info("Getting language statistics");

        try {
            LanguageStatsDTO stats = languageService.getLanguageStatistics();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Language statistics retrieved successfully")
                    .build()
                    .addData("total", stats.getTotal())
                    .addData("adminEnabled", stats.getAdminEnabled())
                    .addData("guestEnabled", stats.getGuestEnabled())
                    .addData("rtlLanguages", stats.getRtlLanguages())
                    .addData("byCharset", stats.getByCharset())
                    .addData("byCurrency", stats.getByCurrency());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving language statistics", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving language statistics", e.getMessage());
        }
    }
}
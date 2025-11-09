package com.tvboot.tivio.translation;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
import com.tvboot.tivio.translation.dto.TranslationDtoWithCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/translations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "🌍 Translations", description = "Multi-language translations for TV clients")
public class TranslationController {

    private final TranslationService translationService;

    // ========== PUBLIC ENDPOINTS FOR TV CLIENTS ==========

    /**
     * Get all translations for a language by ISO code (e.g., "en", "ar", "fr")
     * Primary endpoint for TV clients on guest check-in
     */
    @GetMapping("/{languageCode}")
    @Cacheable(value = "translations", key = "#languageCode")
    @Operation(
            summary = "Get translations by language code",
            description = """
            Returns all translations for TV client UI in specified language.
            Use ISO 639-1 codes (en, fr, ar, etc.)
            
            Example: GET /api/translations/en
            """
    )
    public ResponseEntity<TvBootHttpResponse> getTranslations(@PathVariable String languageCode) {
        log.info("Fetching translations for language: {}", languageCode);

        Map<String, String> translations = translationService.getTranslationsForLanguage(languageCode);

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("Translations retrieved successfully")
                .build()
                .addData("translations", translations)
                .addData("languageCode", languageCode)
                .addCount(translations.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Alternative: Get translations by language ID
     */
    @GetMapping("/by-language-id/{languageId}")
    @Cacheable(value = "translations", key = "'lang_' + #languageId")
    @Operation(summary = "Get translations by language ID")
    public ResponseEntity<TvBootHttpResponse> getTranslationsByLanguageId(@PathVariable Long languageId) {
        log.info("Fetching translations for language ID: {}", languageId);

        Map<String, String> translations = translationService.getTranslationsForLanguage(languageId);

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("Translations retrieved successfully")
                .build()
                .addData("translations", translations)
                .addData("languageId", languageId)
                .addCount(translations.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Optional: Get translations by namespace for modular loading
     * E.g., only load "menu.*" translations initially
     */
    @GetMapping("/{languageCode}/namespace/{namespace}")
    @Cacheable(value = "translations", key = "#languageCode + '_' + #namespace")
    @Operation(
            summary = "Get translations by namespace",
            description = "Load only specific translation namespace (e.g., 'menu', 'settings') for performance"
    )
    public ResponseEntity<TvBootHttpResponse> getTranslationsByNamespace(
            @PathVariable String languageCode,
            @PathVariable String namespace) {

        log.info("Fetching {} namespace translations for language: {}", namespace, languageCode);

        Map<String, String> translations = translationService.getTranslationsByNamespace(languageCode, namespace);

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("Namespace translations retrieved successfully")
                .build()
                .addData("translations", translations)
                .addData("namespace", namespace)
                .addData("languageCode", languageCode)
                .addCount(translations.size());

        return ResponseEntity.ok(response);
    }

    // ========== ADMIN ENDPOINTS WITH DTO ==========

    /**
     * Get all translations for a language as DTO list (Admin)
     */
    @GetMapping("/{languageCode}/dto")
    @Operation(
            summary = "Get translations as DTO list (Admin)",
            description = "Returns translations with language code included - useful for admin/management UI"
    )
    public ResponseEntity<TvBootHttpResponse> getTranslationsAsDto(@PathVariable String languageCode) {
        log.info("Fetching translation DTOs for language: {}", languageCode);

        List<TranslationDtoWithCode> translations = translationService.getTranslationsAsDtoList(languageCode);

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("Translation DTOs retrieved successfully")
                .build()
                .addData("translations", translations)
                .addData("languageCode", languageCode)
                .addCount(translations.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get all translations across all languages (Admin)
     */
    @GetMapping("/all")
    @Operation(
            summary = "Get all translations (Admin)",
            description = "Returns all translations across all languages with language codes"
    )
    public ResponseEntity<TvBootHttpResponse> getAllTranslations() {
        log.info("Fetching all translations");

        List<TranslationDtoWithCode> translations = translationService.getAllTranslationsAsDto();

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("All translations retrieved successfully")
                .build()
                .addData("translations", translations)
                .addCount(translations.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single translation by language code and message key (Admin)
     */
    @GetMapping("/{languageCode}/key/{messageKey}")
    @Operation(
            summary = "Get single translation by key (Admin)",
            description = "Returns a specific translation by language code and message key"
    )
    public ResponseEntity<TvBootHttpResponse> getTranslation(
            @PathVariable String languageCode,
            @PathVariable String messageKey) {

        log.info("Fetching translation: {} for language: {}", messageKey, languageCode);

        TranslationDtoWithCode translation = translationService.getTranslationDto(languageCode, messageKey);

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("Translation retrieved successfully")
                .build()
                .addData("translation", translation);

        return ResponseEntity.ok(response);
    }

    /**
     * Create or update a translation (Admin)
     */
    @PostMapping
    @Operation(
            summary = "Create or update translation (Admin)",
            description = "Creates a new translation or updates existing one"
    )
    public ResponseEntity<TvBootHttpResponse> createOrUpdateTranslation(@Valid @RequestBody TranslationDtoWithCode dto) {
        log.info("Creating/updating translation: {} for language: {}", dto.getMessageKey(), dto.getIso6391());

        TranslationDtoWithCode savedTranslation = translationService.createOrUpdateTranslation(dto);

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("Translation saved successfully")
                .build()
                .addData("translation", savedTranslation);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Bulk import translations for a language (Admin)
     */
    @PostMapping("/{languageCode}/bulk")
    @Operation(
            summary = "Bulk import translations (Admin)",
            description = "Import multiple translations at once for a specific language"
    )
    public ResponseEntity<TvBootHttpResponse> bulkImportTranslations(
            @PathVariable String languageCode,
            @RequestBody Map<String, String> translations) {

        log.info("Bulk importing {} translations for language: {}", translations.size(), languageCode);

        translationService.bulkImportTranslations(languageCode, translations);

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("Translations imported successfully")
                .build()
                .addData("languageCode", languageCode)
                .addCount(translations.size());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete a translation (Admin)
     */
    @DeleteMapping("/{languageCode}/key/{messageKey}")
    @Operation(
            summary = "Delete translation (Admin)",
            description = "Delete a specific translation by language code and message key"
    )
    public ResponseEntity<TvBootHttpResponse> deleteTranslation(
            @PathVariable String languageCode,
            @PathVariable String messageKey) {

        log.info("Deleting translation: {} for language: {}", messageKey, languageCode);

        translationService.deleteTranslation(languageCode, messageKey);

        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("Translation deleted successfully")
                .build()
                .addData("languageCode", languageCode)
                .addData("messageKey", messageKey);

        return ResponseEntity.ok(response);
    }
}
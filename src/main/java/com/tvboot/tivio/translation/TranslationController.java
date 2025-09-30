package com.tvboot.tivio.translation;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/translations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "🌐 Translations", description = "Multi-language translations for TV clients")
public class TranslationController {

    private final TranslationService translationService;

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
    public ResponseEntity<TvBootHttpResponse> getTranslations(
            @PathVariable String languageCode) {
        try {
            log.info("Fetching translations for language: {}", languageCode);
            Map<String, String> translations = translationService
                    .getTranslationsForLanguage(languageCode);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Translations retrieved successfully")
                    .build()
                    .addData("translations", translations)
                    .addData("languageCode", languageCode)
                    .addCount(translations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching translations for language: {}", languageCode, e);
            return TvBootHttpResponse.notFoundResponse(
                    "Translations not found for language: " + languageCode);
        }
    }

    /**
     * Alternative: Get translations by language ID
     */
    @GetMapping("/by-language-id/{languageId}")
    @Cacheable(value = "translations", key = "'lang_' + #languageId")
    @Operation(summary = "Get translations by language ID")
    public ResponseEntity<TvBootHttpResponse> getTranslationsByLanguageId(
            @PathVariable Long languageId) {
        try {
            log.info("Fetching translations for language ID: {}", languageId);
            Map<String, String> translations = translationService
                    .getTranslationsForLanguage(languageId);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Translations retrieved successfully")
                    .build()
                    .addData("translations", translations)
                    .addData("languageId", languageId)
                    .addCount(translations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching translations for language ID: {}", languageId, e);
            return TvBootHttpResponse.notFoundResponse(
                    "Translations not found for language ID: " + languageId);
        }
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
        try {
            log.info("Fetching {} namespace translations for language: {}",
                    namespace, languageCode);

            Map<String, String> translations = translationService
                    .getTranslationsByNamespace(languageCode, namespace);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Namespace translations retrieved successfully")
                    .build()
                    .addData("translations", translations)
                    .addData("namespace", namespace)
                    .addData("languageCode", languageCode)
                    .addCount(translations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching {} namespace for language: {}",
                    namespace, languageCode, e);
            return TvBootHttpResponse.notFoundResponse(
                    "Translations not found for namespace: " + namespace);
        }
    }
}
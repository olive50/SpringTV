package com.tvboot.tivio.language.translation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// TranslationController.java
@RestController
@RequestMapping("/api/translations")
@CrossOrigin(origins = "*")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @GetMapping("/{languageCode}")
    public ResponseEntity<Map<String, String>> getTranslations(@PathVariable String languageCode) {
        try {
            Map<String, String> translations = translationService.getTranslationsForLanguage(languageCode);
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-language-id/{languageId}")
    public ResponseEntity<Map<String, String>> getTranslations(@PathVariable Long languageId) {
        try {
            Map<String, String> translations = translationService.getTranslationsForLanguage(languageId);
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
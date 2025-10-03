package com.tvboot.tivio.translation;

import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.language.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final LanguageRepository languageRepository;

    /**
     * Get all translations for a language by ISO code (cached for performance)
     */
    @Cacheable(value = "translations", key = "#languageCode")
    @Transactional(readOnly = true)
    public Map<String, String> getTranslationsForLanguage(String languageCode) {
        log.debug("Loading translations for language: {}", languageCode);

        List<Translation> translations = translationRepository
                .findByLanguageCode(languageCode);

        if (translations.isEmpty()) {
            log.warn("No translations found for language: {}", languageCode);
            throw new RuntimeException("No translations found for language: " + languageCode);
        }

        Map<String, String> translationMap = translations.stream()
                .collect(Collectors.toMap(
                        Translation::getMessageKey,
                        Translation::getMessageValue,
                        (existing, replacement) -> {
                            log.warn("Duplicate key found, keeping existing value");
                            return existing;
                        }
                ));

        log.info("Loaded {} translations for language: {}",
                translationMap.size(), languageCode);

        return translationMap;
    }

    /**
     * Get translations by language ID
     */
    @Cacheable(value = "translations", key = "'lang_' + #languageId")
    @Transactional(readOnly = true)
    public Map<String, String> getTranslationsForLanguage(Long languageId) {
        log.debug("Loading translations for language ID: {}", languageId);

        List<Translation> translations = translationRepository
                .findByLanguageId(languageId);

        if (translations.isEmpty()) {
            throw new RuntimeException("No translations found for language ID: " + languageId);
        }

        return translations.stream()
                .collect(Collectors.toMap(
                        Translation::getMessageKey,
                        Translation::getMessageValue,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Get translations by namespace prefix
     * E.g., namespace "menu" returns all keys starting with "menu."
     */
    @Transactional(readOnly = true)
    public Map<String, String> getTranslationsByNamespace(
            String languageCode,
            String namespace) {

        log.debug("Loading {} namespace for language: {}", namespace, languageCode);

        List<Translation> translations = translationRepository
                .findByLanguageCode(languageCode);

        String prefix = namespace + ".";

        Map<String, String> namespaceTranslations = translations.stream()
                .filter(t -> t.getMessageKey().startsWith(prefix))
                .collect(Collectors.toMap(
                        Translation::getMessageKey,
                        Translation::getMessageValue
                ));

        log.info("Loaded {} translations for namespace: {}",
                namespaceTranslations.size(), namespace);

        return namespaceTranslations;
    }

    /**
     * Admin function: Create or update a single translation
     */
    @Transactional
    @CacheEvict(value = "translations", allEntries = true)
    public Translation createOrUpdateTranslation(
            String languageCode,
            String key,
            String value) {

        Language language = languageRepository.findByIso6391(languageCode)
                .orElseThrow(() -> new RuntimeException(
                        "Language not found: " + languageCode));

        Optional<Translation> existing = translationRepository
                .findByLanguageIdAndMessageKey(language.getId(), key);

        if (existing.isPresent()) {
            Translation translation = existing.get();
            translation.setMessageValue(value);
            log.info("Updated translation: {} = {}", key, value);
            return translationRepository.save(translation);
        } else {
            Translation translation = Translation.builder()
                    .language(language)
                    .messageKey(key)
                    .messageValue(value)
                    .build();
            log.info("Created translation: {} = {}", key, value);
            return translationRepository.save(translation);
        }
    }

    /**
     * Admin function: Bulk import translations for a language
     */
    @Transactional
    @CacheEvict(value = "translations", allEntries = true)
    public void bulkImportTranslations(
            String languageCode,
            Map<String, String> translations) {

        Language language = languageRepository.findByIso6391(languageCode)
                .orElseThrow(() -> new RuntimeException(
                        "Language not found: " + languageCode));

        List<Translation> translationEntities = translations.entrySet().stream()
                .map(entry -> Translation.builder()
                        .language(language)
                        .messageKey(entry.getKey())
                        .messageValue(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        translationRepository.saveAll(translationEntities);

        log.info("Bulk imported {} translations for language: {}",
                translations.size(), languageCode);
    }
}
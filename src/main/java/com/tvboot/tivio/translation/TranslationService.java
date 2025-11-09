package com.tvboot.tivio.translation;

import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.language.LanguageRepository;
import com.tvboot.tivio.translation.dto.TranslationDtoWithCode;
import com.tvboot.tivio.translation.dto.TranslationMapper;
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
    private final TranslationMapper translationMapper;

    /**
     * Get all translations for a language by ISO code (cached for performance)
     * Returns Map<String, String> for TV client consumption
     */
    @Cacheable(value = "translations", key = "#languageCode")
    @Transactional(readOnly = true)
    public Map<String, String> getTranslationsForLanguage(String languageCode) {
        log.debug("Loading translations for language: {}", languageCode);

        List<Translation> translations = translationRepository
                .findByLanguageCode(languageCode);

        if (translations.isEmpty()) {
            log.warn("No translations found for language: {}", languageCode);
            throw new ResourceNotFoundException("Translation", "languageCode", languageCode);
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
            throw new ResourceNotFoundException("Translation", "languageId", languageId);
        }

        return translations.stream()
                .collect(Collectors.toMap(
                        Translation::getMessageKey,
                        Translation::getMessageValue,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Get translations as DTOs with language code (for admin/management)
     */
    @Cacheable(value = "translationsDto", key = "#languageCode")
    @Transactional(readOnly = true)
    public List<TranslationDtoWithCode> getTranslationsAsDtoList(String languageCode) {
        log.debug("Loading translation DTOs for language: {}", languageCode);

        List<Translation> translations = translationRepository
                .findByLanguageCode(languageCode);

        if (translations.isEmpty()) {
            log.warn("No translations found for language: {}", languageCode);
            throw new ResourceNotFoundException("Translation", "languageCode", languageCode);
        }

        List<TranslationDtoWithCode> dtoList = translationMapper.toDtoList(translations);
        log.info("Loaded {} translation DTOs for language: {}", dtoList.size(), languageCode);

        return dtoList;
    }

    /**
     * Get all translations across all languages as DTOs (admin function)
     */
    @Transactional(readOnly = true)
    public List<TranslationDtoWithCode> getAllTranslationsAsDto() {
        log.debug("Loading all translations as DTOs");
        List<Translation> allTranslations = translationRepository.findAll();
        return translationMapper.toDtoList(allTranslations);
    }

    /**
     * Get a single translation by language code and key
     */
    @Transactional(readOnly = true)
    public TranslationDtoWithCode getTranslationDto(String languageCode, String messageKey) {
        Translation translation = translationRepository
                .findByLanguage_Iso6391AndMessageKey(languageCode, messageKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Translation not found for key '%s' in language '%s'", messageKey, languageCode)
                ));

        return translationMapper.toDto(translation);
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

        if (translations.isEmpty()) {
            throw new ResourceNotFoundException("Translation", "languageCode", languageCode);
        }

        String prefix = namespace + ".";

        Map<String, String> namespaceTranslations = translations.stream()
                .filter(t -> t.getMessageKey().startsWith(prefix))
                .collect(Collectors.toMap(
                        Translation::getMessageKey,
                        Translation::getMessageValue
                ));

        if (namespaceTranslations.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("No translations found for namespace '%s' in language '%s'", namespace, languageCode)
            );
        }

        log.info("Loaded {} translations for namespace: {}",
                namespaceTranslations.size(), namespace);

        return namespaceTranslations;
    }

    /**
     * Admin function: Create or update a single translation using DTO
     */
    @Transactional
    @CacheEvict(value = {"translations", "translationsDto"}, allEntries = true)
    public TranslationDtoWithCode createOrUpdateTranslation(TranslationDtoWithCode dto) {

        Language language = languageRepository.findByIso6391(dto.getIso6391())
                .orElseThrow(() -> new ResourceNotFoundException("Language", "iso6391", dto.getIso6391()));

        Optional<Translation> existing = translationRepository
                .findByLanguageIdAndMessageKey(language.getId(), dto.getMessageKey());

        Translation translation;
        if (existing.isPresent()) {
            translation = existing.get();
            translation.setMessageValue(dto.getMessageValue());
            log.info("Updated translation: {} = {}", dto.getMessageKey(), dto.getMessageValue());
        } else {
            translation = Translation.builder()
                    .language(language)
                    .messageKey(dto.getMessageKey())
                    .messageValue(dto.getMessageValue())
                    .build();
            log.info("Created translation: {} = {}", dto.getMessageKey(), dto.getMessageValue());
        }

        translation = translationRepository.save(translation);
        return translationMapper.toDto(translation);
    }

    /**
     * Admin function: Create or update a single translation (legacy method)
     */
    @Transactional
    @CacheEvict(value = {"translations", "translationsDto"}, allEntries = true)
    public Translation createOrUpdateTranslation(
            String languageCode,
            String key,
            String value) {

        Language language = languageRepository.findByIso6391(languageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "iso6391", languageCode));

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
    @CacheEvict(value = {"translations", "translationsDto"}, allEntries = true)
    public void bulkImportTranslations(
            String languageCode,
            Map<String, String> translations) {

        Language language = languageRepository.findByIso6391(languageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "iso6391", languageCode));

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

    /**
     * Admin function: Delete a translation
     */
    @Transactional
    @CacheEvict(value = {"translations", "translationsDto"}, allEntries = true)
    public void deleteTranslation(String languageCode, String messageKey) {
        Language language = languageRepository.findByIso6391(languageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "iso6391", languageCode));

        // Verify translation exists before deleting
        translationRepository.findByLanguageIdAndMessageKey(language.getId(), messageKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Translation not found for key '%s' in language '%s'", messageKey, languageCode)
                ));

        translationRepository.deleteByLanguageIdAndMessageKey(language.getId(), messageKey);
        log.info("Deleted translation: {} for language: {}", messageKey, languageCode);
    }
}
package com.tvboot.tivio.translation;

import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.language.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final LanguageRepository languageRepository;

    public Map<String, String> getTranslationsForLanguage(String languageCode) {
        List<Translation> translations = translationRepository.findByLanguageCode(languageCode);

        return translations.stream()
                .collect(Collectors.toMap(
                        Translation::getMessageKey,
                        Translation::getMessageValue
                ));
    }

    public Map<String, String> getTranslationsForLanguage(Long languageId) {
        List<Translation> translations = translationRepository.findByLanguageId(languageId);

        return translations.stream()
                .collect(Collectors.toMap(
                        Translation::getMessageKey,
                        Translation::getMessageValue
                ));
    }

    @Transactional
    public Translation createOrUpdateTranslation(String languageCode, String key, String value) {
        Language language = languageRepository.findByIso6391(languageCode)
                .orElseThrow(() -> new RuntimeException("Language not found: " + languageCode));

        return createOrUpdateTranslation(language, key, value);
    }

    @Transactional
    public Translation createOrUpdateTranslation(Language language, String key, String value) {
        Optional<Translation> existing = translationRepository
                .findByLanguageIdAndMessageKey(language.getId(), key);

        if (existing.isPresent()) {
            Translation translation = existing.get();
            translation.setMessageValue(value);
            return translationRepository.save(translation);
        } else {
            Translation translation = Translation.builder()
                    .language(language)
                    .messageKey(key)
                    .messageValue(value)
                    .build();
            return translationRepository.save(translation);
        }
    }

    @Transactional
    public void updateTranslationProgress(Language language) {
        long translationCount = translationRepository.countByLanguageId(language.getId());
        // You might have a different way to calculate progress


        languageRepository.save(language);
    }

    private int calculateProgress(long translationCount) {
        // Implement your progress calculation logic
        // For example: (currentCount / expectedTotal) * 100
        return Math.min(100, (int) (translationCount * 100 / 500)); // Example calculation
    }
}
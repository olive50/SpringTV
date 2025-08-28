package com.tvboot.tivio.service;

import com.tvboot.tivio.entities.Language;
import com.tvboot.tivio.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LanguageService {

    private final LanguageRepository languageRepository;

    public List<Language> getAllLanguages() {
        return languageRepository.findAll();
    }

    public Optional<Language> getLanguageById(Long id) {
        return languageRepository.findById(id);
    }

    public Language createLanguage(Language language) {
        if (languageRepository.findByCode(language.getCode()).isPresent()) {
            throw new RuntimeException("Language code already exists: " + language.getCode());
        }
        if (languageRepository.findByName(language.getName()).isPresent()) {
            throw new RuntimeException("Language name already exists: " + language.getName());
        }
        return languageRepository.save(language);
    }

    public Language updateLanguage(Long id, Language languageDetails) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Language not found: " + id));

        // Check uniqueness
        Optional<Language> existingByCode = languageRepository.findByCode(languageDetails.getCode());
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(id)) {
            throw new RuntimeException("Language code already exists: " + languageDetails.getCode());
        }

        Optional<Language> existingByName = languageRepository.findByName(languageDetails.getName());
        if (existingByName.isPresent() && !existingByName.get().getId().equals(id)) {
            throw new RuntimeException("Language name already exists: " + languageDetails.getName());
        }

        language.setName(languageDetails.getName());
        language.setCode(languageDetails.getCode());

        return languageRepository.save(language);
    }

    public void deleteLanguage(Long id) {
        if (!languageRepository.existsById(id)) {
            throw new RuntimeException("Language not found: " + id);
        }
        languageRepository.deleteById(id);
    }
}
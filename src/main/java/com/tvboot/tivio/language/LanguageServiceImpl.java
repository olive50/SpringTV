package com.tvboot.tivio.language;

import com.tvboot.tivio.common.exception.BusinessException;
import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.common.util.FileStorageService;
import com.tvboot.tivio.language.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    @Autowired
    private final FileStorageService fileStorageService;

    private static final String FLAG_DIR = "image/flags";

    @Override
    @Transactional(readOnly = true)
    public Page<Language> getLanguages(int page, int size) {
        log.debug("Getting languages - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("displayOrder").ascending().and(Sort.by("name")));
        return languageRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Language> getAdminEnabledLanguages(int page, int size) {
        log.debug("Getting admin enabled languages - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return languageRepository.findByIsAdminEnabledTrueOrderByDisplayOrderAscNameAsc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Language> getGuestEnabledLanguages(int page, int size) {
        log.debug("Getting guest enabled languages - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return languageRepository.findByIsGuestEnabledTrueOrderByDisplayOrderAscNameAsc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Language> searchLanguages(String search, int page, int size) {
        log.debug("Searching languages with term: '{}' - page: {}, size: {}", search, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return languageRepository.searchLanguages(search, pageable);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Language> getLanguages(int page, int size, String searchQuery,
                                   Boolean isAdminEnabled,
                                   Boolean isGuestEnabled,
                                   Boolean isRtl) {
            Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.ASC, "displayOrder", "name"));

            Specification<Language> spec = LanguageSpecification.withFilters(
            searchQuery, isAdminEnabled, isGuestEnabled, isRtl
         );

        return languageRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLanguages() {
        return languageRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAdminEnabledLanguages() {
        return languageRepository.countByIsAdminEnabledTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countGuestEnabledLanguages() {
        return languageRepository.countByIsGuestEnabledTrue();
    }

    @Override
    public Language createLanguage(LanguageCreateDTO createDTO) {
        log.info("Creating new language: {}", createDTO.getName());

        validateUniqueIso6391(createDTO.getIso6391(), null);
        validateUniqueDisplayOrder(createDTO.getDisplayOrder(), null);

        Language language = languageMapper.toEntity(createDTO);
        return languageRepository.save(language);
    }

    @Override
    public LanguageResponseDTO updateLanguage(Long id, LanguageUpdateDTO updateDTO) {
        log.info("Updating language with ID: {}", id);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "id", id));

        updateLanguageFields(language, updateDTO, id);

        Language savedLanguage = languageRepository.save(language);
        return languageMapper.toDTO(savedLanguage);
    }

    @Override
    public void deleteLanguage(Long id) {
        log.info("Deleting language with id: {}", id);

        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "id", id));

        if (language.getFlagPath() != null) {
            fileStorageService.deleteFile(language.getFlagPath());
        }

        languageRepository.delete(language);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Language> getLanguageById(Long id) {
        return languageRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Language> getLanguageByIso6391(String iso6391) {
        return languageRepository.findByIso6391(iso6391);
    }

    @Override
    public LanguageResponseDTO createLanguageWithFlag(LanguageCreateDTO createDTO, MultipartFile flagFile) {
        Language language = languageMapper.toEntity(createDTO);

        if (flagFile != null && !flagFile.isEmpty()) {

            String customFilename = fileStorageService.generateLanguageFlagFilename(
                    language.getIso6391(),
                    flagFile.getOriginalFilename()
            );


            String flagPath = fileStorageService.storeFileWithCustomName(flagFile, FLAG_DIR, customFilename);
            language.setFlagPath(customFilename);
            log.info("Saved flag with custom name: {}", flagPath);
        }

        language = languageRepository.save(language);
        return languageMapper.toDTO(language);
    }

    @Override
    public LanguageResponseDTO updateLanguageWithFlag(Long id, LanguageUpdateDTO updateDTO, MultipartFile flagFile) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language", "id", id));

        String oldFlagPath = FLAG_DIR+language.getFlagPath();

        updateLanguageFields(language, updateDTO, id);

        if (flagFile != null && !flagFile.isEmpty()) {
            if (oldFlagPath != null) {
                fileStorageService.deleteFile(oldFlagPath);
            }

            String customFilename = fileStorageService.generateLanguageFlagFilename(
                    language.getIso6391(),
                    flagFile.getOriginalFilename()
            );



            String newFlagPath = fileStorageService.storeFileWithCustomName(flagFile, FLAG_DIR, customFilename);
            language.setFlagPath(customFilename);
        }

        Language savedLanguage = languageRepository.save(language);
        return languageMapper.toDTO(savedLanguage);
    }

    @Override
    public LanguageStatsDTO getLanguageStatistics() {
        long total = languageRepository.count();
        long adminEnabled = languageRepository.countByIsAdminEnabledTrue();
        long guestEnabled = languageRepository.countByIsGuestEnabledTrue();
        long rtlLanguages = languageRepository.countByIsRtlTrue();

        Map<String, Long> charsetStats = new HashMap<>();
        List<Object[]> charsetResults = languageRepository.countByCharset();
        for (Object[] result : charsetResults) {
            charsetStats.put((String) result[0], (Long) result[1]);
        }

        Map<String, Long> currencyStats = new HashMap<>();
        List<Object[]> currencyResults = languageRepository.countByCurrency();
        for (Object[] result : currencyResults) {
            currencyStats.put((String) result[0], (Long) result[1]);
        }

        return LanguageStatsDTO.builder()
                .total(total)
                .adminEnabled(adminEnabled)
                .guestEnabled(guestEnabled)
                .rtlLanguages(rtlLanguages)
                .byCharset(charsetStats)
                .byCurrency(currencyStats)
                .build();
    }

    // Private validation methods
    private void validateUniqueIso6391(String iso6391, Long excludeId) {
        Optional<Language> existing = languageRepository.findByIso6391(iso6391);
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new BusinessException("ISO 639-1 code already exists: " + iso6391);
        }
    }

    private void validateUniqueDisplayOrder(Integer displayOrder, Long excludeId) {
        if (displayOrder == null) return;

        Optional<Language> existing = languageRepository.findByDisplayOrder(displayOrder);
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new BusinessException("Display order already exists: " + displayOrder);
        }
    }

    private void updateLanguageFields(Language language, LanguageUpdateDTO updateDTO, Long id) {
        // Use MapStruct for automatic mapping with null-safe updates
        languageMapper.updateEntityFromDTO(updateDTO, language);

        // Handle specific validations that require ID context
        if (updateDTO.getDisplayOrder() != null) {
            validateUniqueDisplayOrder(updateDTO.getDisplayOrder(), id);
        }
    }
}

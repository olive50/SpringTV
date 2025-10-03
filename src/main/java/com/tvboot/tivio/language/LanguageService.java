package com.tvboot.tivio.language;

import com.tvboot.tivio.language.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface LanguageService {

    // Pagination methods
    Page<Language> getLanguages(int page, int size);
    Page<Language> getAdminEnabledLanguages(int page, int size);
    Page<Language> getGuestEnabledLanguages(int page, int size);
    Page<Language> searchLanguages(String search, int page, int size);
    Page<Language> getLanguages(int page, int size, String q, Boolean isAdminEnabled,
                                Boolean isGuestEnabled, Boolean isRtl);

    // Count methods
    long countLanguages();
    long countAdminEnabledLanguages();
    long countGuestEnabledLanguages();

    // CRUD operations
    Language createLanguage(LanguageCreateDTO createDTO);
    LanguageResponseDTO updateLanguage(Long id, LanguageUpdateDTO updateDTO);
    void deleteLanguage(Long id);
    Optional<Language> getLanguageById(Long id);
    Optional<Language> getLanguageByIso6391(String iso6391);

    // File operations
    LanguageResponseDTO createLanguageWithFlag(LanguageCreateDTO createDTO, MultipartFile flagFile);
    LanguageResponseDTO updateLanguageWithFlag(Long id, LanguageUpdateDTO updateDTO, MultipartFile flagFile);

    // Statistics
    LanguageStatsDTO getLanguageStatistics();
}
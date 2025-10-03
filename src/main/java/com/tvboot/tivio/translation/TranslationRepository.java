package com.tvboot.tivio.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {

    // Find all translations for a specific language
    List<Translation> findByLanguageId(Long languageId);

    // Find all translations for a language by its code
    @Query("SELECT t FROM Translation t WHERE t.language.iso6391 = :languageCode")
    List<Translation> findByLanguageCode(@Param("languageCode") String languageCode);

    // Find a specific translation by language and key
    Optional<Translation> findByLanguageIdAndMessageKey(Long languageId, String messageKey);

    Optional<Translation> findByLanguage_Iso6391AndMessageKey(String languageCode, String messageKey);

    // Check if a translation exists for a language and key
    boolean existsByLanguageIdAndMessageKey(Long languageId, String messageKey);
    boolean existsByLanguage_Iso6391AndMessageKey(String languageCode, String messageKey);

    // Count translations per language
    long countByLanguageId(Long languageId);
    long countByLanguage_Iso6391(String languageCode);

    // Bulk operations
    List<Translation> findByMessageKey(String messageKey);
    void deleteByLanguageIdAndMessageKey(Long languageId, String messageKey);
}
package com.tvboot.tivio.language;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long>, JpaSpecificationExecutor<Language> {

    Optional<Language> findByIso6391(String iso6391);

    Page<Language> findByIsAdminEnabledTrueOrderByDisplayOrderAscNameAsc(Pageable pageable);
    Page<Language> findByIsGuestEnabledTrueOrderByDisplayOrderAscNameAsc(Pageable pageable);

    // Search functionality
    @Query("SELECT l FROM Language l WHERE " +
            "LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.nativeName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.iso6391) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY l.displayOrder ASC, l.name ASC")
    Page<Language> searchLanguages(@Param("search") String search, Pageable pageable);

    // Advanced filtering
  /*  @Query("SELECT l FROM Language l WHERE " +
            "(:q IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
            "(:isAdminEnabled IS NULL OR l.isAdminEnabled = :isAdminEnabled) AND " +
            "(:isGuestEnabled IS NULL OR l.isGuestEnabled = :isGuestEnabled) AND " +
            "(:isRtl IS NULL OR l.isRtl = :isRtl) " +
            "ORDER BY l.displayOrder ASC, l.name ASC")
    Page<Language> findAllWithFilters(@Param("q") String q,
                                      @Param("isAdminEnabled") Boolean isAdminEnabled,
                                      @Param("isGuestEnabled") Boolean isGuestEnabled,
                                      @Param("isRtl") Boolean isRtl,
                                      Pageable pageable);*/

    // Count methods
    long countByIsAdminEnabledTrue();
    long countByIsGuestEnabledTrue();
    long countByIsRtlTrue();

    // Statistics queries
    @Query("SELECT l.charset as charset, COUNT(l) as count " +
            "FROM Language l " +
            "WHERE l.charset IS NOT NULL " +
            "GROUP BY l.charset")
    List<Object[]> countByCharset();

    @Query("SELECT l.currencyCode as currency, COUNT(l) as count " +
            "FROM Language l " +
            "WHERE l.currencyCode IS NOT NULL " +
            "GROUP BY l.currencyCode")
    List<Object[]> countByCurrency();

    Optional<Language> findByDisplayOrder(Integer displayOrder);
}
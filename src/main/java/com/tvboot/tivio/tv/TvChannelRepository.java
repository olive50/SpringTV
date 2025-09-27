package com.tvboot.tivio.tv;


import com.tvboot.tivio.language.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TvChannelRepository extends JpaRepository<TvChannel, Long> {

    Optional<TvChannel> findByChannelNumber(int channelNumber);

    List<TvChannel> findByCategoryId(Long categoryId);

    List<TvChannel> findByLanguageId(Long languageId);

    // Basic pagination methods
    Page<TvChannel> findByActiveTrue(Pageable pageable);

    Page<TvChannel> findByActiveTrueOrderBySortOrderAscNameAsc(Pageable pageable);

    // Count methods
    long countByActiveTrue();

//    long countByActiveTrueAndCategory(TvChannelCategory category);
    long countByActiveTrueAndCategory_name(String categoryName);

    // Find by category with pagination
    Page<TvChannel> findByActiveTrueAndCategoryOrderBySortOrderAscNameAsc(
            String category, Pageable pageable);

    // Find channels available for guests

    long countByActiveTrueAndAvailableTrue();



    // Find by language
    Page<TvChannel> findByLanguage_name(
            String language, Pageable pageable);

    // Search functionality
    @Query("SELECT c FROM TvChannel c WHERE c.active = true AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(c.channelNumber AS string) LIKE CONCAT('%', :search, '%')) " +
            "ORDER BY c.name ASC")
    Page<TvChannel> searchChannels(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(c) FROM TvChannel c WHERE c.active = true AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(c.channelNumber AS string) LIKE CONCAT('%', :search, '%'))")
    long countSearchChannels(@Param("search") String search);

    // Find by tvChannel number
    Optional<TvChannel> findByChannelNumberAndActiveTrue(Integer channelNumber);

    // Get all categories
    @Query("SELECT DISTINCT c.category FROM TvChannel c WHERE c.active = true ORDER BY c.category.name")
    List<String> findAllCategories();

    // Get all languages
    @Query("SELECT DISTINCT c.language FROM TvChannel c WHERE c.active = true ORDER BY c.language.name")
    List<String> findAllLanguages();

    Page<TvChannel> findByActive(boolean active, Pageable pageable);

    long countByActive(boolean active);

    // ✅ If you later want HD and Available queries:

    Page<TvChannel> findByAvailable(boolean available, Pageable pageable);
    @Query("SELECT c FROM TvChannel c WHERE c.ip = :ip AND c.port = :port")
    Optional<TvChannel> findByIpAndPort(@Param("ip") String ip, @Param("port") int port);

    // Count queries
    @Query("SELECT COUNT(c) FROM TvChannel c WHERE c.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(c) FROM TvChannel c WHERE c.language.id = :languageId")
    long countByLanguageId(@Param("languageId") Long languageId);

    // Stats queries
    @Query("SELECT c.category.name as category, COUNT(c) as count " +
            "FROM TvChannel c " +
            "WHERE c.category IS NOT NULL " +
            "GROUP BY c.category.id, c.category.name")
    List<Object[]> countByCategory();

    @Query("SELECT c.language.name as language, COUNT(c) as count " +
            "FROM TvChannel c " +
            "WHERE c.language IS NOT NULL " +
            "GROUP BY c.language.id, c.language.name")
    List<Object[]> countByLanguage();

    Page<TvChannel> findByActiveTrueAndAvailableTrueOrderBySortOrderAscNameAsc(Pageable pageable);


    Page<TvChannel> findByActiveTrueAndLanguageOrderBySortOrderAscNameAsc(Language language, Pageable pageable);

    Page<TvChannel> findByActiveTrueAndLanguage_NameOrderBySortOrderAscNameAsc(String language, Pageable pageable);

    @Query("""
    SELECT c FROM TvChannel c
    WHERE (:q IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')))
      AND (:categoryId IS NULL OR c.category.id = :categoryId)
      AND (:languageId IS NULL OR c.language.id = :languageId)
      AND (:isActive IS NULL OR c.active = :isActive)
""")
    Page<TvChannel> findAllWithFilters(String q, Long categoryId, Long languageId, Boolean isActive, Pageable pageable);

    Optional<TvChannel> findBySortOrder(int sortOrder);
}

package com.tvboot.tivio.tv;


import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.tv.tvcategory.TvChannelCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<TvChannel, Long> {

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



    Page<TvChannel> findByActiveTrueAndAvailableTrueOrderBySortOrderAscNameAsc(Pageable pageable);


    Page<TvChannel> findByActiveTrueAndLanguageOrderBySortOrderAscNameAsc(String language, Pageable pageable);

}

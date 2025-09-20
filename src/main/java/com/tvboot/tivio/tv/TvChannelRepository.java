package com.tvboot.tivio.tv;

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
    Page<TvChannel> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT c FROM TvChannel c WHERE c.category.id = :categoryId AND c.language.id = :languageId")
    List<TvChannel> findByCategoryAndLanguage(@Param("categoryId") Long categoryId, @Param("languageId") Long languageId);

    @Query("SELECT c FROM TvChannel c WHERE c.ip = :ip AND c.port = :port")
    Optional<TvChannel> findByIpAndPort(@Param("ip") String ip, @Param("port") int port);

    Page<TvChannel> findByCategoryAndActive(String category, Boolean active, Pageable pageable);

    Page<TvChannel> findByCategory(String category, Pageable pageable);

    Page<TvChannel> findByActive(Boolean active, Pageable pageable);

 // for stats
    // Count queries
    long countByActive(boolean active);

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
}
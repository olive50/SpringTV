package com.tvboot.tivio.repository;

import com.tvboot.tivio.entities.TvChannel;
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
}
package com.tvboot.tivio.repository;

import com.tvboot.tivio.entities.ChannelPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelPackageRepository extends JpaRepository<ChannelPackage, Long> {
    Optional<ChannelPackage> findByName(String name);
    List<ChannelPackage> findByIsActiveTrue();
    List<ChannelPackage> findByIsPremiumTrue();

    @Query("SELECT cp FROM ChannelPackage cp JOIN cp.channels c WHERE c.id = :channelId")
    List<ChannelPackage> findPackagesWithChannel(@Param("channelId") Long channelId);
}
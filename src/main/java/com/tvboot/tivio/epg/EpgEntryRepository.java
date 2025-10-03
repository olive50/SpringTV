package com.tvboot.tivio.epg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EpgEntryRepository extends JpaRepository<EpgEntry, Long> {
    List<EpgEntry> findByChannelIdOrderByStartTime(Long channelId);

    @Query("SELECT e FROM EpgEntry e WHERE e.channel.id = :channelId AND e.startTime <= :time AND e.endTime > :time")
    List<EpgEntry> findCurrentProgram(@Param("channelId") Long channelId, @Param("time") LocalDateTime time);

    @Query("SELECT e FROM EpgEntry e WHERE e.channel.id = :channelId AND e.startTime >= :startTime AND e.endTime <= :endTime ORDER BY e.startTime")
    List<EpgEntry> findByChannelAndTimeRange(@Param("channelId") Long channelId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
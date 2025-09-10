package com.tvboot.tivio.terminal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// src/main/java/com/tvboot/iptv/repository/TerminalRepository.java
@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long>, JpaSpecificationExecutor<Terminal> {

    Optional<Terminal> findByTerminalId(String terminalId);
    Optional<Terminal> findByMacAddress(String macAddress);
    Optional<Terminal> findByIpAddress(String ipAddress);

    boolean existsByTerminalId(String terminalId);
    boolean existsByMacAddress(String macAddress);
    boolean existsByIpAddressAndIdNot(String ipAddress, Long id);

    List<Terminal> findByStatus(TerminalStatus status);
    List<Terminal> findByDeviceType(DeviceType deviceType);
    List<Terminal> findByLocationContainingIgnoreCase(String location);
    List<Terminal> findByRoomId(Long roomId);

    @Query("SELECT t FROM Terminal t WHERE t.lastSeen < :threshold")
    List<Terminal> findInactiveTerminals(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(t) FROM Terminal t WHERE t.status = :status")
    long countByStatus(@Param("status") TerminalStatus status);

    @Query("SELECT t.deviceType, COUNT(t) FROM Terminal t GROUP BY t.deviceType")
    List<Object[]> countByDeviceType();

    @Query("SELECT t.location, COUNT(t) FROM Terminal t GROUP BY t.location")
    List<Object[]> countByLocation();

    @Query("SELECT AVG(t.uptime) FROM Terminal t WHERE t.uptime IS NOT NULL")
    Double getAverageUptime();

    @Query("SELECT AVG(t.responseTime) FROM Terminal t WHERE t.responseTime IS NOT NULL")
    Double getAverageResponseTime();
}
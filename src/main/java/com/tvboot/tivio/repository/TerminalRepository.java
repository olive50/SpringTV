
package com.tvboot.tivio.repository;

import com.tvboot.tivio.entities.Terminal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {
    Optional<Terminal> findByTerminalId(String terminalId);
    Optional<Terminal> findByMacAddress(String macAddress);
    Optional<Terminal> findBySerialNumber(String serialNumber);

    List<Terminal> findByDeviceType(Terminal.DeviceType deviceType);
    List<Terminal> findByStatus(Terminal.TerminalStatus status);
    List<Terminal> findByRoomId(Long roomId);

    Page<Terminal> findByDeviceTypeAndStatus(Terminal.DeviceType deviceType, Terminal.TerminalStatus status, Pageable pageable);

    @Query("SELECT t FROM Terminal t WHERE t.lastSeen < :threshold")
    List<Terminal> findOfflineTerminals(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT t FROM Terminal t WHERE t.warrantyExpiry <= :date")
    List<Terminal> findExpiredWarranties(@Param("date") LocalDateTime date);

    @Query("SELECT t FROM Terminal t WHERE t.location LIKE %:location%")
    List<Terminal> findByLocationContaining(@Param("location") String location);
}
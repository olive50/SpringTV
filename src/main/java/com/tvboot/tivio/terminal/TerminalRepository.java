package com.tvboot.tivio.terminal;

import com.tvboot.tivio.common.enumeration.DeviceType;
import com.tvboot.tivio.common.enumeration.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long>, JpaSpecificationExecutor<Terminal> {

    Optional<Terminal> findByTerminalCode(String terminalCode);
    Optional<Terminal> findByMacAddress(String macAddress);
    Optional<Terminal> findByIpAddress(String ipAddress);

    boolean existsByTerminalCode(String terminalCode);
    boolean existsByMacAddress(String macAddress);
    boolean existsByIpAddressAndIdNot(String ipAddress, Long id);

    List<Terminal> findByActive(Boolean active);
    List<Terminal> findByDeviceType(DeviceType deviceType);

    List<Terminal> findByLocationIdentifier(String roomNumber);

    @Query("SELECT t FROM Terminal t WHERE t.lastSeen < :threshold")
    List<Terminal> findInactiveTerminals(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(t) FROM Terminal t WHERE t.active = :active")
    long countByActive(@Param("active") Boolean active);

    @Query("SELECT t.deviceType, COUNT(t) FROM Terminal t GROUP BY t.deviceType")
    List<Object[]> countByDeviceType();

    @Query("SELECT t.locationType, COUNT(t) FROM Terminal t GROUP BY t.locationType")
    List<Object[]> countByLocationType();

    @Query("SELECT AVG(t.uptime) FROM Terminal t WHERE t.uptime IS NOT NULL")
    Double getAverageUptime();

    Optional<Terminal> findByMacAddressAndTerminalCode(String macAddress, String terminalCode);
    List<Terminal> findByLocationTypeAndLocationIdentifier(
            LocationType locationType,
            String locationIdentifier
    );

    List<Terminal> findByLocationType(LocationType locationType);

    // Pour récupérer tous les terminaux dans les chambres
    @Query("SELECT t FROM Terminal t WHERE t.locationType = 'ROOM'")
    List<Terminal> findAllRoomTerminals();

    // Pour récupérer les terminaux d'une chambre spécifique
    @Query("SELECT t FROM Terminal t WHERE t.locationType = 'ROOM' AND t.locationIdentifier = :roomNumber")
    List<Terminal> findByRoomNumber(@Param("roomNumber") String roomNumber);

}
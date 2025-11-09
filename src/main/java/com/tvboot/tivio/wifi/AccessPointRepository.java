package com.tvboot.tivio.wifi;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccessPointRepository extends JpaRepository<AccessPoint, Long> {
    Optional<AccessPoint> findByTerminalId(Long terminalId);
}
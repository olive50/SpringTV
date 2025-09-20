package com.tvboot.tivio.terminal;

import com.tvboot.tivio.entities.TerminalChannelAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalChannelAssignmentRepository extends JpaRepository<TerminalChannelAssignment, Long> {
    List<TerminalChannelAssignment> findByTerminalId(Long terminalId);
    List<TerminalChannelAssignment> findByChannelId(Long channelId);

    @Query("SELECT tca FROM TerminalChannelAssignment tca WHERE tca.terminal.id = :terminalId AND tca.isEnabled = true ORDER BY tca.position")
    List<TerminalChannelAssignment> findEnabledChannelsByTerminal(@Param("terminalId") Long terminalId);

    Optional<TerminalChannelAssignment> findByTerminalIdAndChannelId(Long terminalId, Long channelId);
}
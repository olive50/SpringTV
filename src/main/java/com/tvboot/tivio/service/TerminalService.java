package com.tvboot.tivio.service;

import com.tvboot.tivio.entities.Terminal;
import com.tvboot.tivio.repository.TerminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TerminalService {

    private final TerminalRepository terminalRepository;

    public List<Terminal> getAllTerminals() {
        return terminalRepository.findAll();
    }

    public Page<Terminal> getAllTerminals(Pageable pageable) {
        return terminalRepository.findAll(pageable);
    }

    public Optional<Terminal> getTerminalById(Long id) {
        return terminalRepository.findById(id);
    }

    public Optional<Terminal> getTerminalByTerminalId(String terminalId) {
        return terminalRepository.findByTerminalId(terminalId);
    }

    public List<Terminal> getTerminalsByDeviceType(Terminal.DeviceType deviceType) {
        return terminalRepository.findByDeviceType(deviceType);
    }

    public List<Terminal> getTerminalsByStatus(Terminal.TerminalStatus status) {
        return terminalRepository.findByStatus(status);
    }

    public List<Terminal> getTerminalsByRoom(Long roomId) {
        return terminalRepository.findByRoomId(roomId);
    }

    public List<Terminal> getOfflineTerminals(int minutesThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesThreshold);
        return terminalRepository.findOfflineTerminals(threshold);
    }

    public Terminal createTerminal(Terminal terminal) {
        // Validate unique constraints
        if (terminalRepository.findByTerminalId(terminal.getTerminalId()).isPresent()) {
            throw new RuntimeException("Terminal ID already exists: " + terminal.getTerminalId());
        }

        if (terminal.getMacAddress() != null &&
                terminalRepository.findByMacAddress(terminal.getMacAddress()).isPresent()) {
            throw new RuntimeException("MAC address already exists: " + terminal.getMacAddress());
        }

        if (terminal.getSerialNumber() != null &&
                terminalRepository.findBySerialNumber(terminal.getSerialNumber()).isPresent()) {
            throw new RuntimeException("Serial number already exists: " + terminal.getSerialNumber());
        }

        terminal.setLastSeen(LocalDateTime.now());
        return terminalRepository.save(terminal);
    }

    public Terminal updateTerminal(Long id, Terminal terminalDetails) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Terminal not found: " + id));

        // Update fields
        if (terminalDetails.getDeviceType() != null) {
            terminal.setDeviceType(terminalDetails.getDeviceType());
        }
        if (terminalDetails.getBrand() != null) {
            terminal.setBrand(terminalDetails.getBrand());
        }
        if (terminalDetails.getModel() != null) {
            terminal.setModel(terminalDetails.getModel());
        }
        if (terminalDetails.getMacAddress() != null) {
            terminal.setMacAddress(terminalDetails.getMacAddress());
        }
        if (terminalDetails.getIpAddress() != null) {
            terminal.setIpAddress(terminalDetails.getIpAddress());
        }
        if (terminalDetails.getFirmwareVersion() != null) {
            terminal.setFirmwareVersion(terminalDetails.getFirmwareVersion());
        }
        if (terminalDetails.getStatus() != null) {
            terminal.setStatus(terminalDetails.getStatus());
        }
        if (terminalDetails.getLocation() != null) {
            terminal.setLocation(terminalDetails.getLocation());
        }
        if (terminalDetails.getWarrantyExpiry() != null) {
            terminal.setWarrantyExpiry(terminalDetails.getWarrantyExpiry());
        }
        if (terminalDetails.getNotes() != null) {
            terminal.setNotes(terminalDetails.getNotes());
        }
        if (terminalDetails.getRoom() != null) {
            terminal.setRoom(terminalDetails.getRoom());
        }

        return terminalRepository.save(terminal);
    }

    public void deleteTerminal(Long id) {
        if (!terminalRepository.existsById(id)) {
            throw new RuntimeException("Terminal not found: " + id);
        }
        terminalRepository.deleteById(id);
    }

    public Terminal updateTerminalStatus(Long id, Terminal.TerminalStatus status) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Terminal not found: " + id));

        terminal.setStatus(status);
        terminal.setLastSeen(LocalDateTime.now());

        return terminalRepository.save(terminal);
    }

    public Terminal updateLastSeen(String terminalId) {
        Terminal terminal = terminalRepository.findByTerminalId(terminalId)
                .orElseThrow(() -> new RuntimeException("Terminal not found: " + terminalId));

        terminal.setLastSeen(LocalDateTime.now());
        return terminalRepository.save(terminal);
    }
}
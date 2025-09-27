package com.tvboot.tivio.terminal;

import com.tvboot.tivio.common.enumeration.DeviceType;
import com.tvboot.tivio.common.enumeration.TerminalStatus;
import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.room.Room;
import com.tvboot.tivio.room.RoomRepository;
import com.tvboot.tivio.terminal.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TerminalService {

    private final TerminalRepository terminalRepository;
    private final RoomRepository roomRepository;
    private final TerminalMapper terminalMapper;
    private final ConnectivityService connectivityService;



    @Transactional(readOnly = true)
    public Page<TerminalDto> getAllTerminals(TerminalSearchCriteria criteria, Pageable pageable) {
        Specification<Terminal> spec = createSpecification(criteria);
        Page<Terminal> terminals = terminalRepository.findAll(spec, pageable);
        return terminals.map(terminalMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<TerminalDto> getAllTerminals() {
        List<Terminal> terminals = terminalRepository.findAll();
        return terminals.stream()
                .map(terminalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TerminalDto getTerminalById(Long id) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terminal not found with ID: " + id));
        return terminalMapper.toDto(terminal);
    }

    public TerminalDto createTerminal(TerminalCreateRequest request) {
        log.info("Creating new terminal: {}", request.getTerminalCode());

        // Validate uniqueness
        validateTerminalUniqueness(request);

        Terminal terminal = Terminal.builder()
                .terminalCode(request.getTerminalCode())
                .deviceType(request.getDeviceType())
                .brand(request.getBrand())
                .model(request.getModel())
                .macAddress(request.getMacAddress().toUpperCase())
                .ipAddress(request.getIpAddress())
                .location(request.getLocation())
                .firmwareVersion(request.getFirmwareVersion())
                .serialNumber(request.getSerialNumber())
                .status(TerminalStatus.INACTIVE)
                .lastSeen(LocalDateTime.now())
                .isOnline(false)
                .build();

        // Set room if provided
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + request.getRoomId()));
            terminal.setRoom(room);
        }

        Terminal savedTerminal = terminalRepository.save(terminal);

        // Test initial connectivity
        testConnectivityAsync(savedTerminal);

        log.info("Terminal created successfully: {}", savedTerminal.getTerminalCode());
        return terminalMapper.toDto(savedTerminal);
    }

    public TerminalDto updateTerminal(Long id, TerminalUpdateRequest request) {
        log.info("Updating terminal with ID: {}", id);

        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terminal not found with ID: " + id));

        // Update fields if provided
        if (request.getTerminalCode() != null) {
            validateTerminalIdUniqueness(request.getTerminalCode(), id);
            terminal.setTerminalCode(request.getTerminalCode());
        }

        if (request.getDeviceType() != null) {
            terminal.setDeviceType(request.getDeviceType());
        }

        if (request.getBrand() != null) {
            terminal.setBrand(request.getBrand());
        }

        if (request.getModel() != null) {
            terminal.setModel(request.getModel());
        }

        if (request.getMacAddress() != null) {
            validateMacAddressUniqueness(request.getMacAddress(), id);
            terminal.setMacAddress(request.getMacAddress().toUpperCase());
        }

        if (request.getIpAddress() != null) {
            validateIpAddressUniqueness(request.getIpAddress(), id);
            terminal.setIpAddress(request.getIpAddress());
        }

        if (request.getStatus() != null) {
            terminal.setStatus(request.getStatus());
        }

        if (request.getLocation() != null) {
            terminal.setLocation(request.getLocation());
        }

        if (request.getFirmwareVersion() != null) {
            terminal.setFirmwareVersion(request.getFirmwareVersion());
        }

        if (request.getSerialNumber() != null) {
            terminal.setSerialNumber(request.getSerialNumber());
        }

        // Update room
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + request.getRoomId()));
            terminal.setRoom(room);
        } else if (request.getRoomId() == null) {
            terminal.setRoom(null);
        }

        Terminal savedTerminal = terminalRepository.save(terminal);

        // Test connectivity if IP changed
        if (request.getIpAddress() != null) {
            testConnectivityAsync(savedTerminal);
        }

        log.info("Terminal updated successfully: {}", savedTerminal.getTerminalCode());
        return terminalMapper.toDto(savedTerminal);
    }

    public void deleteTerminal(Long id) {
        log.info("Deleting terminal with ID: {}", id);

        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terminal not found with ID: " + id));

        terminalRepository.delete(terminal);
        log.info("Terminal deleted successfully: {}", terminal.getTerminalCode());
    }

    public ConnectivityTestResult testTerminalConnectivity(Long id) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal not found with ID: " + id));

        return testConnectivity(terminal);
    }

    public ConnectivityTestResult testConnectivity(Terminal terminal) {
        try {
            log.debug("Testing connectivity for terminal: {}", terminal.getTerminalCode());

            ConnectivityTestResult result = connectivityService.pingHost(terminal.getIpAddress());

            // Update terminal status based on result
            terminal.setIsOnline(result.getSuccess());


            if (result.getSuccess()) {
                terminal.setLastSeen(LocalDateTime.now());
                if (terminal.getStatus() == TerminalStatus.OFFLINE) {
                    terminal.setStatus(TerminalStatus.ACTIVE);
                }
            } else {
                if (terminal.getStatus() == TerminalStatus.ACTIVE) {
                    terminal.setStatus(TerminalStatus.OFFLINE);
                }
            }

            terminalRepository.save(terminal);
            return result;

        } catch (Exception e) {
            log.error("Error testing connectivity for terminal {}: {}", terminal.getTerminalCode(), e.getMessage());
            return ConnectivityTestResult.builder()
                    .success(false)
                    .message("Connectivity test failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Async
    public void testConnectivityAsync(Terminal terminal) {
        testConnectivity(terminal);
    }

    public void rebootTerminal(Long id) {
        Terminal terminal = terminalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terminal not found with ID: " + id));

        // In a real implementation, this would send a reboot command to the terminal
        // For now, we'll simulate by changing status
        terminal.setStatus(TerminalStatus.MAINTENANCE);
        terminalRepository.save(terminal);

        // Simulate reboot process (in real app, this would be handled by the terminal)
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            terminal.setStatus(TerminalStatus.ACTIVE);
            terminal.setLastSeen(LocalDateTime.now());
            terminalRepository.save(terminal);
        });
    }

    @Transactional(readOnly = true)
    public TerminalStatsDto getTerminalStatistics() {
        Map<String, Long> statusCounts = Arrays.stream(TerminalStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        terminalRepository::countByStatus
                ));

        Map<String, Long> deviceTypeCounts = terminalRepository.countByDeviceType()
                .stream()
                .collect(Collectors.toMap(
                        row -> ((DeviceType) row[0]).name(),
                        row -> (Long) row[1]
                ));

        Map<String, Long> locationCounts = terminalRepository.countByLocation()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        return TerminalStatsDto.builder()
                .total(terminalRepository.count())
                .active(statusCounts.getOrDefault(TerminalStatus.ACTIVE.name(), 0L))
                .inactive(statusCounts.getOrDefault(TerminalStatus.INACTIVE.name(), 0L))
                .offline(statusCounts.getOrDefault(TerminalStatus.OFFLINE.name(), 0L))
                .maintenance(statusCounts.getOrDefault(TerminalStatus.MAINTENANCE.name(), 0L))
                .faulty(statusCounts.getOrDefault(TerminalStatus.FAULTY.name(), 0L))
                .byDeviceType(deviceTypeCounts)
                .byLocation(locationCounts)
                .averageUptime(terminalRepository.getAverageUptime())
                .build();
    }

    public void updateTerminalHeartbeat(String macAddress) {
        terminalRepository.findByMacAddress(macAddress.toUpperCase())
                .ifPresent(terminal -> {
                    terminal.setLastSeen(LocalDateTime.now());
                    terminal.setIsOnline(true);
                    if (terminal.getStatus() == TerminalStatus.OFFLINE) {
                        terminal.setStatus(TerminalStatus.ACTIVE);
                    }
                    terminalRepository.save(terminal);
                });
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkTerminalStatus() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<Terminal> inactiveTerminals = terminalRepository.findInactiveTerminals(threshold);

        for (Terminal terminal : inactiveTerminals) {
            if (terminal.getStatus() == TerminalStatus.ACTIVE) {
                terminal.setStatus(TerminalStatus.OFFLINE);
                terminal.setIsOnline(false);
                terminalRepository.save(terminal);
                log.warn("Terminal {} marked as offline due to inactivity", terminal.getTerminalCode());
            }
        }
    }

    private void validateTerminalUniqueness(TerminalCreateRequest request) {
        if (terminalRepository.existsByTerminalCode(request.getTerminalCode())) {
            throw new IllegalArgumentException("Terminal ID already exists: " + request.getTerminalCode());
        }

        if (terminalRepository.existsByMacAddress(request.getMacAddress().toUpperCase())) {
            throw new IllegalArgumentException("MAC address already exists: " + request.getMacAddress());
        }

        if (terminalRepository.findByIpAddress(request.getIpAddress()).isPresent()) {
            throw new IllegalArgumentException("IP address already exists: " + request.getIpAddress());
        }
    }

    private void validateTerminalIdUniqueness(String terminalCode, Long excludeId) {
        terminalRepository.findByTerminalCode(terminalCode)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(excludeId)) {
                        throw new IllegalArgumentException("Terminal ID already exists: " + terminalCode);
                    }
                });
    }

    private void validateMacAddressUniqueness(String macAddress, Long excludeId) {
        terminalRepository.findByMacAddress(macAddress.toUpperCase())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(excludeId)) {
                        throw new IllegalArgumentException("MAC address already exists: " + macAddress);
                    }
                });
    }

    private void validateIpAddressUniqueness(String ipAddress, Long excludeId) {
        if (terminalRepository.existsByIpAddressAndIdNot(ipAddress, excludeId)) {
            throw new IllegalArgumentException("IP address already exists: " + ipAddress);
        }
    }

    private Specification<Terminal> createSpecification(TerminalSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + criteria.getSearch().toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("terminalCode")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("ipAddress")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("macAddress")), searchTerm)
                );
                predicates.add(searchPredicate);
            }

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getDeviceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("deviceType"), criteria.getDeviceType()));
            }

            if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + criteria.getLocation().toLowerCase() + "%"
                ));
            }

            if (criteria.getRoomId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("room").get("id"), criteria.getRoomId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
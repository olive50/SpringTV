package com.tvboot.tivio.terminal;

import com.tvboot.tivio.common.enumeration.DeviceType;
import com.tvboot.tivio.common.enumeration.LocationType;
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
        System.out.println(request);
        Terminal terminal = terminalMapper.toEntity(request);

        // Handle location association
        if ("ROOM".equalsIgnoreCase(request.getLocationType())) {
            Room room = roomRepository.findByRoomNumber(request.getLocationIdentifier())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Room not found with number: " + request.getLocationIdentifier()
                    ));
            terminal.setRoom(room);
        } else if ("LOBBY".equalsIgnoreCase(request.getLocationType())) {
            // Example: handle another type (optional)
            // Lobby lobby = lobbyRepository.findByIdentifier(request.getLocationIdentifier())
            //         .orElseThrow(() -> new ResourceNotFoundException("Lobby not found"));
            // device.setLobby(lobby);
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

        terminalMapper.updateFromRequest(terminal, request);


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
                if (!terminal.getIsOnline()) {
                    terminal.setIsOnline(true);
                }
            } else {
                if (terminal.getIsOnline()) {
                    terminal.setIsOnline(false);
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

        terminalRepository.save(terminal);

        // Simulate reboot process (in real app, this would be handled by the terminal)
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {

            terminal.setLastSeen(LocalDateTime.now());
            terminalRepository.save(terminal);
        });
    }


    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkTerminalStatus() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<Terminal> inactiveTerminals = terminalRepository.findInactiveTerminals(threshold);

        for (Terminal terminal : inactiveTerminals) {
            if (terminal.getIsOnline()) {
                terminal.setIsOnline(false);
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

            if (criteria.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getIsActive()));
            }

            if (criteria.getDeviceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("deviceType"), criteria.getDeviceType()));
            }



            if (criteria.getLocationId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("room").get("id"), criteria.getLocationId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public TerminalStatsDto  getTerminalStatistics(){
        return new TerminalStatsDto();
    }

    public void updateTerminalHeartbeat(String macAddress) {
        terminalRepository.findByMacAddress(macAddress.toUpperCase())
                .ifPresent(terminal -> {
                    terminal.setLastSeen(LocalDateTime.now());
                    terminal.setIsOnline(true);
                    if (!terminal.getIsOnline()) {
                        terminal.setIsOnline(true);
                    }
                    terminalRepository.save(terminal);
                });
    }

    public void assignTerminalToRoom(Long terminalId, Long roomId) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        terminal.setLocationType(LocationType.ROOM);
        terminal.setLocationIdentifier(room.getRoomNumber());

        terminalRepository.save(terminal);
        log.info("Terminal {} assigned to room {}", terminal.getTerminalCode(), room.getRoomNumber());
    }

    public void assignTerminalToLocation(Long terminalId, LocationType locationType, String locationIdentifier) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal not found"));

        terminal.setLocationType(locationType);
        terminal.setLocationIdentifier(locationIdentifier);

        terminalRepository.save(terminal);
        log.info("Terminal {} assigned to {} {}",
                terminal.getTerminalCode(), locationType, locationIdentifier);
    }

    public List<TerminalDto> getTerminalsByLocation(LocationType locationType, String locationIdentifier) {
        List<Terminal> terminals = terminalRepository
                .findByLocationTypeAndLocationIdentifier(locationType, locationIdentifier);

        return terminals.stream()
                .map(terminalMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds a Terminal by ID and sets its active status to true.
     * @param terminalId The ID of the terminal to activate.
     * @return The activated Terminal object.
     * @throws RuntimeException if the terminal is not found.
     */
    @Transactional // Ensures the operation is atomic and the change is committed
    public Terminal activateTerminal(Long terminalId) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal with ID " + terminalId + " not found."));

        // Set the active field to true
        terminal.setActive(true);

        // Save the updated terminal back to the database (JPA handles the update)
        // Note: With @Transactional, often the save is not explicitly needed as the entity is managed,
        // but explicitly calling save is safer and clearer.
        return terminalRepository.save(terminal);
    }



}
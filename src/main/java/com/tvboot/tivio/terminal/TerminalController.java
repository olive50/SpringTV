package com.tvboot.tivio.terminal;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
import com.tvboot.tivio.common.enumeration.LocationType;
import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.room.Room;
import com.tvboot.tivio.room.RoomRepository;
import com.tvboot.tivio.room.RoomService;
import com.tvboot.tivio.terminal.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// src/main/java/com/tvboot/iptv/controller/TerminalController.java
@RestController
@RequestMapping("/terminals")
@Validated
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TerminalController {

    private final TerminalService terminalService;
    private final RoomRepository roomRepository;
    private final TerminalMapper terminalMapper;



    @PostMapping("/{terminalId}/assign")
    public ResponseEntity<?> assignTerminal(
            @PathVariable Long terminalId,
            @RequestBody AssignTerminalRequest request) {

        if (request.getLocationType() == LocationType.ROOM) {
            // Vérifier que la chambre existe
            Room room = roomRepository.findByRoomNumber(request.getLocationIdentifier())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        }

        terminalService.assignTerminalToLocation(
                terminalId,
                request.getLocationType(),
                request.getLocationIdentifier()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<TvBootHttpResponse> getAllTerminals() {
        log.info("Getting all terminals");

        try {
            List<TerminalDto> terminals = terminalService.getAllTerminals();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminals retrieved successfully")
                    .build()
                    .addData("terminals", terminals)
                    .addCount(terminals.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving terminals", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve terminals",
                    e.getMessage()
            );
        }
    }

    @GetMapping
    public ResponseEntity<TvBootHttpResponse> getTerminalsPaged(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "terminalCode") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            TerminalSearchCriteria criteria) {

        log.info("Getting paged terminals - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<TerminalDto> terminalsPage = terminalService.getAllTerminals(criteria, pageable);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminals retrieved successfully")
                    .build()
                    .addData("terminals", terminalsPage.getContent())
                    .addPagination(page, size, terminalsPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving paged terminals", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve terminals",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> getTerminalById(@PathVariable Long id) {
        log.info("Getting terminal by ID: {}", id);

        try {
            TerminalDto terminal = terminalService.getTerminalById(id);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminal retrieved successfully")
                    .build()
                    .addData("terminal", terminal);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Terminal not found with ID: {}", id);
            return TvBootHttpResponse.notFoundResponse("Terminal not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error retrieving terminal {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve terminal",
                    e.getMessage()
            );
        }
    }

    @PostMapping
    public ResponseEntity<TvBootHttpResponse> createTerminal(@Valid @RequestBody TerminalCreateRequest request) {
        log.info("Creating terminal with code: {}", request.getTerminalCode());

        try {
            TerminalDto terminal = terminalService.createTerminal(request);

            TvBootHttpResponse response = TvBootHttpResponse.created()
                    .message("Terminal created successfully")
                    .build()
                    .addData("terminal", terminal);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid terminal data: {}", e.getMessage());
            return TvBootHttpResponse.badRequestResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating terminal", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to create terminal",
                    e.getMessage()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> updateTerminal(
            @PathVariable Long id,
            @Valid @RequestBody TerminalUpdateRequest request) {

        log.info("Updating terminal with ID: {}", id);

        try {
            TerminalDto terminal = terminalService.updateTerminal(id, request);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminal updated successfully")
                    .build()
                    .addData("terminal", terminal);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Terminal not found for update with ID: {}", id);
            return TvBootHttpResponse.notFoundResponse("Terminal not found with ID: " + id);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid terminal update data: {}", e.getMessage());
            return TvBootHttpResponse.badRequestResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating terminal {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to update terminal",
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> deleteTerminal(@PathVariable Long id) {
        log.info("Deleting terminal with ID: {}", id);

        try {
            terminalService.deleteTerminal(id);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminal deleted successfully")
                    .build();

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Terminal not found for deletion with ID: {}", id);
            return TvBootHttpResponse.notFoundResponse("Terminal not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error deleting terminal {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to delete terminal",
                    e.getMessage()
            );
        }
    }

    @PostMapping("/{id}/test-connectivity")
    public ResponseEntity<TvBootHttpResponse> testConnectivity(@PathVariable Long id) {
        log.info("Testing connectivity for terminal ID: {}", id);

        try {
            ConnectivityTestResult result = terminalService.testTerminalConnectivity(id);

            String message = result.getSuccess()
                    ? "Terminal connectivity test successful"
                    : "Terminal connectivity test failed";

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message(message)
                    .build()
                    .addData("connectivityResult", result);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Terminal not found for connectivity test with ID: {}", id);
            return TvBootHttpResponse.notFoundResponse("Terminal not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error testing terminal connectivity {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to test terminal connectivity",
                    e.getMessage()
            );
        }
    }

    @PostMapping("/{id}/reboot")
    public ResponseEntity<TvBootHttpResponse> rebootTerminal(@PathVariable Long id) {
        log.info("Rebooting terminal with ID: {}", id);

        try {
            terminalService.rebootTerminal(id);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminal reboot initiated successfully")
                    .build()
                    .addData("terminalId", id);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Terminal not found for reboot with ID: {}", id);
            return TvBootHttpResponse.notFoundResponse("Terminal not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error rebooting terminal {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to reboot terminal",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<TvBootHttpResponse> getTerminalStatistics() {
        log.info("Getting terminal statistics");

        try {
            TerminalStatsDto stats = terminalService.getTerminalStatistics();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminal statistics retrieved successfully")
                    .build()
                    .addData("statistics", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving terminal statistics", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve terminal statistics",
                    e.getMessage()
            );
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<TvBootHttpResponse> updateHeartbeat(@RequestParam String macAddress) {
        log.info("Updating heartbeat for MAC address: {}", macAddress);

        try {
            terminalService.updateTerminalHeartbeat(macAddress);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminal heartbeat updated successfully")
                    .build()
                    .addData("macAddress", macAddress);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating terminal heartbeat for MAC {}", macAddress, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to update terminal heartbeat",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/{id}/connectivity")
    public ResponseEntity<TvBootHttpResponse> getTerminalConnectivity(@PathVariable Long id) {
        log.info("Checking connectivity for terminal ID: {}", id);

        try {
            ConnectivityTestResult result = terminalService.testTerminalConnectivity(id);

            String message = result.getSuccess()
                    ? "Terminal connectivity check completed - Online"
                    : "Terminal connectivity check completed - Offline";

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message(message)
                    .build()
                    .addData("connectivityResult", result)
                    .addData("terminalId", id);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Terminal not found for connectivity check with ID: {}", id);
            return TvBootHttpResponse.notFoundResponse("Terminal not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error checking terminal connectivity {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to check terminal connectivity",
                    e.getMessage()
            );
        }
    }

    // Additional IPTV-specific endpoint
    @PostMapping("/{id}/activate")
    public ResponseEntity<TvBootHttpResponse> activateTerminal(@PathVariable Long id) {
        log.info("Authorizing device with terminal ID: {}", id);

        Terminal activatedTerminal = terminalService.activateTerminal(id);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Device authorized successfully")
                    .build()
                    .addDevice(activatedTerminal);

            return ResponseEntity.ok(response);

    }

    // IPTV-specific endpoint for getting terminals by room
    @GetMapping("/room/{roomId}")
    public ResponseEntity<TvBootHttpResponse> getTerminalsByRoom(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting terminals for room ID: {} - page: {}, size: {}", roomId, page, size);

        try {
            // This would need to be implemented in the service
            // Page<TerminalDto> terminalsPage = terminalService.getTerminalsByRoom(roomId, page, size);
            // List<TerminalDto> terminals = terminalsPage.getContent();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Room terminals retrieved successfully")
                    .build()
                    .addRoom(roomId);
            // .addData("terminals", terminals)
            // .addPagination(page, size, terminalsPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving terminals for room {}", roomId, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve room terminals",
                    e.getMessage()
            );
        }
    }

    // Get terminals by device type (useful for IPTV management)
    @GetMapping("/type/{deviceType}")
    public ResponseEntity<TvBootHttpResponse> getTerminalsByDeviceType(
            @PathVariable String deviceType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting terminals by device type: {} - page: {}, size: {}", deviceType, page, size);

        try {
            // This would need to be implemented in the service
            // Page<TerminalDto> terminalsPage = terminalService.getTerminalsByDeviceType(deviceType, page, size);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Terminals by device type retrieved successfully")
                    .build()
                    .addData("deviceType", deviceType);
            // .addData("terminals", terminalsPage.getContent())
            // .addPagination(page, size, terminalsPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving terminals by device type {}", deviceType, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve terminals by device type",
                    e.getMessage()
            );
        }
    }
}
package com.tvboot.tivio.terminal;

import com.tvboot.tivio.terminal.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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

// src/main/java/com/tvboot/iptv/controller/TerminalController.java
@RestController
@RequestMapping("/api/terminals")
@Validated
@Slf4j
public class TerminalController {

    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @GetMapping
    public ResponseEntity<List<TerminalDto>> getAllTerminals() {
        List<TerminalDto> terminals = terminalService.getAllTerminals();
        return ResponseEntity.ok(terminals);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<TerminalDto>> getTerminalsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "terminalId") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            TerminalSearchCriteria criteria) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<TerminalDto> terminals = terminalService.getAllTerminals(criteria, pageable);
        return ResponseEntity.ok(terminals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerminalDto> getTerminalById(@PathVariable Long id) {
        TerminalDto terminal = terminalService.getTerminalById(id);
        return ResponseEntity.ok(terminal);
    }

    @PostMapping
    public ResponseEntity<TerminalDto> createTerminal(@Valid @RequestBody TerminalCreateRequest request) {
        TerminalDto terminal = terminalService.createTerminal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(terminal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerminalDto> updateTerminal(
            @PathVariable Long id,
            @Valid @RequestBody TerminalUpdateRequest request) {
        TerminalDto terminal = terminalService.updateTerminal(id, request);
        return ResponseEntity.ok(terminal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTerminal(@PathVariable Long id) {
        terminalService.deleteTerminal(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test-connectivity")
    public ResponseEntity<ConnectivityTestResult> testConnectivity(@PathVariable Long id) {
        ConnectivityTestResult result = terminalService.testTerminalConnectivity(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reboot")
    public ResponseEntity<Void> rebootTerminal(@PathVariable Long id) {
        terminalService.rebootTerminal(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<TerminalStatsDto> getTerminalStatistics() {
        TerminalStatsDto stats = terminalService.getTerminalStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> updateHeartbeat(@RequestParam String macAddress) {
        terminalService.updateTerminalHeartbeat(macAddress);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/connectivity")
    public ResponseEntity<ConnectivityTestResult> getTerminalConnectivity(@PathVariable Long id) {
        try {
            ConnectivityTestResult result = terminalService.testTerminalConnectivity(id);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
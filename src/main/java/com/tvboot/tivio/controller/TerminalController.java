package com.tvboot.tivio.controller;

import com.tvboot.tivio.entities.Terminal;
import com.tvboot.tivio.service.TerminalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/terminals")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class TerminalController {

    private final TerminalService terminalService;

    @GetMapping
    public ResponseEntity<List<Terminal>> getAllTerminals() {
        List<Terminal> terminals = terminalService.getAllTerminals();
        return ResponseEntity.ok(terminals);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Terminal>> getAllTerminalsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "terminalId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Terminal> terminals = terminalService.getAllTerminals(pageable);
        return ResponseEntity.ok(terminals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Terminal> getTerminalById(@PathVariable Long id) {
        return terminalService.getTerminalById(id)
                .map(terminal -> ResponseEntity.ok(terminal))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/terminal-id/{terminalId}")
    public ResponseEntity<Terminal> getTerminalByTerminalId(@PathVariable String terminalId) {
        return terminalService.getTerminalByTerminalId(terminalId)
                .map(terminal -> ResponseEntity.ok(terminal))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/device-type/{deviceType}")
    public ResponseEntity<List<Terminal>> getTerminalsByDeviceType(@PathVariable Terminal.DeviceType deviceType) {
        List<Terminal> terminals = terminalService.getTerminalsByDeviceType(deviceType);
        return ResponseEntity.ok(terminals);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Terminal>> getTerminalsByStatus(@PathVariable Terminal.TerminalStatus status) {
        List<Terminal> terminals = terminalService.getTerminalsByStatus(status);
        return ResponseEntity.ok(terminals);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Terminal>> getTerminalsByRoom(@PathVariable Long roomId) {
        List<Terminal> terminals = terminalService.getTerminalsByRoom(roomId);
        return ResponseEntity.ok(terminals);
    }

    @GetMapping("/offline")
    public ResponseEntity<List<Terminal>> getOfflineTerminals(
            @RequestParam(defaultValue = "30") int minutesThreshold) {
        List<Terminal> terminals = terminalService.getOfflineTerminals(minutesThreshold);
        return ResponseEntity.ok(terminals);
    }

    @PostMapping
    public ResponseEntity<Terminal> createTerminal(@Valid @RequestBody Terminal terminal) {
        try {
            Terminal createdTerminal = terminalService.createTerminal(terminal);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTerminal);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Terminal> updateTerminal(
            @PathVariable Long id,
            @Valid @RequestBody Terminal terminalDetails) {
        try {
            Terminal updatedTerminal = terminalService.updateTerminal(id, terminalDetails);
            return ResponseEntity.ok(updatedTerminal);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Terminal> updateTerminalStatus(
            @PathVariable Long id,
            @RequestParam Terminal.TerminalStatus status) {
        try {
            Terminal updatedTerminal = terminalService.updateTerminalStatus(id, status);
            return ResponseEntity.ok(updatedTerminal);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/heartbeat/{terminalId}")
    public ResponseEntity<Terminal> updateLastSeen(@PathVariable String terminalId) {
        try {
            Terminal updatedTerminal = terminalService.updateLastSeen(terminalId);
            return ResponseEntity.ok(updatedTerminal);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTerminal(@PathVariable Long id) {
        try {
            terminalService.deleteTerminal(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
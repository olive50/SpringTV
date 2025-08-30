package com.tvboot.tivio.controller;

import com.tvboot.tivio.audit.Auditable;
import com.tvboot.tivio.dto.*;
import com.tvboot.tivio.service.TvChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Chaînes TV", description = "Gestion des chaînes de télévision")
@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class TvChannelController {

    private final TvChannelService tvChannelService;

    @Operation(
            summary = "Liste des chaînes",
            description = "Récupère toutes les chaînes TV avec leurs catégories et langues"
    )
    @GetMapping
    @Auditable(action = "LIST_CHANNELS", resource = "TV_CHANNEL")
    public ResponseEntity<List<TvChannelDTO>> getAllChannels() {
        log.debug("Received request to get all TV channels");
        List<TvChannelDTO> channels = tvChannelService.getAllChannels();
        log.debug("Returning {} TV channels", channels.size());
        return ResponseEntity.ok(channels);
    }

    @Operation(
            summary = "Chaînes paginées",
            description = "Récupère les chaînes TV avec pagination et tri"
    )
    @GetMapping("/paged")
    public ResponseEntity<Page<TvChannelDTO>> getAllChannelsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "channelNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TvChannelDTO> channels = tvChannelService.getAllChannels(pageable);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TvChannelDTO> getChannelById(@PathVariable Long id) {
        return tvChannelService.getChannelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{channelNumber}")
    public ResponseEntity<TvChannelDTO> getChannelByNumber(@PathVariable int channelNumber) {
        return tvChannelService.getChannelByNumber(channelNumber)
                .map(channel -> ResponseEntity.ok(channel))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TvChannelDTO>> getChannelsByCategory(@PathVariable Long categoryId) {
        List<TvChannelDTO> channels = tvChannelService.getChannelsByCategory(categoryId);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/language/{languageId}")
    public ResponseEntity<List<TvChannelDTO>> getChannelsByLanguage(@PathVariable Long languageId) {
        List<TvChannelDTO> channels = tvChannelService.getChannelsByLanguage(languageId);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TvChannelDTO>> searchChannels(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TvChannelDTO> channels = tvChannelService.searchChannelsByName(name, pageable);
        return ResponseEntity.ok(channels);
    }

    @Auditable(action = "CREATE_CHANNEL", resource = "TV_CHANNEL", logParams = true, logResult = true)
    public ResponseEntity<TvChannelDTO> createChannel(@Valid @RequestBody TvChannelCreateDTO createDTO) {
        log.info("Received request to create TV channel: {}", createDTO.getName());

        try {
            TvChannelDTO createdChannel = tvChannelService.createChannel(createDTO);
            log.info("Successfully created TV channel with ID: {}", createdChannel.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);

        } catch (Exception e) {
            log.error("Failed to create TV channel: {}", createDTO.getName(), e);
            throw e;
        }
    }

    @Operation(summary = "Modifier une chaîne")
    @PutMapping("/{id}")
    @Auditable(action = "UPDATE_CHANNEL", resource = "TV_CHANNEL", logParams = true)
    public ResponseEntity<TvChannelDTO> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody TvChannelUpdateDTO updateDTO) {

        log.info("Received request to update TV channel with ID: {}", id);

        try {
            TvChannelDTO updatedChannel = tvChannelService.updateChannel(id, updateDTO);
            log.info("Successfully updated TV channel: {}", updatedChannel.getName());
            return ResponseEntity.ok(updatedChannel);

        } catch (Exception e) {
            log.error("Failed to update TV channel with ID: {}", id, e);
            throw e;
        }
    }

    @Operation(summary = "Supprimer une chaîne")
    @DeleteMapping("/{id}")
    @Auditable(action = "DELETE_CHANNEL", resource = "TV_CHANNEL")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long id) {
        log.info("Received request to delete TV channel with ID: {}", id);

        try {
            tvChannelService.deleteChannel(id);
            log.info("Successfully deleted TV channel with ID: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Failed to delete TV channel with ID: {}", id, e);
            throw e;
        }
    }
}
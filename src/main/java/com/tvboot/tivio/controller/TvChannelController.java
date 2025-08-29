package com.tvboot.tivio.controller;

import com.tvboot.tivio.dto.*;
import com.tvboot.tivio.service.TvChannelService;
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
@RequestMapping("/api/channels")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class TvChannelController {

    private final TvChannelService tvChannelService;

    @GetMapping
    public ResponseEntity<List<TvChannelDTO>> getAllChannels() {
        List<TvChannelDTO> channels = tvChannelService.getAllChannels();
        return ResponseEntity.ok(channels);
    }

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

    @PostMapping
    public ResponseEntity<TvChannelDTO> createChannel(@Valid @RequestBody TvChannelCreateDTO createDTO) {
        try {
            TvChannelDTO createdChannel = tvChannelService.createChannel(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TvChannelDTO> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody TvChannelUpdateDTO updateDTO) {
        try {
            TvChannelDTO updatedChannel = tvChannelService.updateChannel(id, updateDTO);
            return ResponseEntity.ok(updatedChannel);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long id) {
        try {
            tvChannelService.deleteChannel(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
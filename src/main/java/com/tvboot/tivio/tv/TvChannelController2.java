package com.tvboot.tivio.tv;

import com.tvboot.tivio.tv.dto.TvChannelCreateDTO;
import com.tvboot.tivio.tv.dto.TvChannelDTO;
import com.tvboot.tivio.tv.dto.TvChannelUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tv-channels")
@CrossOrigin(origins = "*")
public class TvChannelController2 {

    @Autowired
    private TvChannelService2 tvChannelService;

    // CREATE - Without Logo
    @PostMapping
    public ResponseEntity<TvChannelDTO> createChannel(@Valid @RequestBody TvChannelCreateDTO createDTO) {
        TvChannelDTO created = tvChannelService.createChannel(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // CREATE - With Logo
    @PostMapping(path = "/with-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TvChannelDTO> createChannelWithLogo(
            @RequestPart("channel") @Valid TvChannelCreateDTO createDTO,
            @RequestPart("logo") MultipartFile logoFile) {
        TvChannelDTO created = tvChannelService.createChannelWithLogo(createDTO, logoFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ - Get All Channels
    @GetMapping
    public ResponseEntity<List<TvChannelDTO>> getAllChannels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active) {
        Page<TvChannelDTO> channels = tvChannelService.getAllChannels(page, size, category, active);
        return ResponseEntity.ok(channels.getContent());
    }

    // READ - Get Single Channel
    @GetMapping("/{id}")
    public ResponseEntity<TvChannelDTO> getChannel(@PathVariable Long id) {
        TvChannelDTO channel = tvChannelService.getChannelById(id);
        return ResponseEntity.ok(channel);
    }

    // UPDATE - Without Logo
    @PutMapping("/{id}")
    public ResponseEntity<TvChannelDTO> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody TvChannelUpdateDTO updateDTO) {
        TvChannelDTO updated = tvChannelService.updateChannel(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    // UPDATE - With Logo
    @PutMapping(path = "/{id}/with-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TvChannelDTO> updateChannelWithLogo(
            @PathVariable Long id,
            @RequestPart("channel") @Valid TvChannelUpdateDTO updateDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {
        TvChannelDTO updated = tvChannelService.updateChannelWithLogo(id, updateDTO, logoFile);
        return ResponseEntity.ok(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long id) {
        tvChannelService.deleteChannel(id);
        return ResponseEntity.noContent().build();
    }

    // Get Logo
    @GetMapping("/{id}/logo")
    public ResponseEntity<Resource> getChannelLogo(@PathVariable Long id) {
        Resource resource = tvChannelService.getChannelLogo(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }
}
package com.tvboot.tivio.tvchannel;

import com.tvboot.tivio.common.audit.Auditable;
import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;


import com.tvboot.tivio.tvchannel.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tvchannels")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*") // Configure as needed for your frontend
class TvChannelController {

    private final TvChannelService tvChannelService;
    private final TvChannelMapper channelMapper;

    /**
     * Get all channels with pagination
     */
    @GetMapping
    ResponseEntity<TvBootHttpResponse> getChannels(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting channels - page: {}, size: {}", page, size);

        try {
            Page<TvChannel> channelPage = tvChannelService.getChannels(page, size);
            long total = tvChannelService.countChannels();

            // Convert entities to DTOs with logo URLs
            List<TvChannelResponseDTO> channelDTOs = channelPage.getContent().stream()
                    .map(channelMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Channels retrieved successfully")
                    .build()
                    .addChannels(channelDTOs)  // Using DTOs instead of entities
                    .addPagination(page, size, total);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving channels", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving channels",
                    e.getMessage()
            );
        }
    }

    /**
     * Get channels by category with pagination
     */
    @GetMapping("/category/{category}")
    ResponseEntity<TvBootHttpResponse> getChannelsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting channels by category: {} - page: {}, size: {}", category, page, size);

        try {
            Page<TvChannel> channelPage = tvChannelService.getChannelsByCategory(category, page, size);
            long total = tvChannelService.countChannelsByCategory(category);

            // Convert entities to DTOs with logo URLs
            List<TvChannelResponseDTO> channelDTOs = channelPage.getContent().stream()
                    .map(channelMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Channels retrieved successfully for category: " + category)
                    .build()
                    .addChannels(channelDTOs)
                    .addPagination(page, size, total)
                    .addData("category", category);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving channels for category: {}", category, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving channels for category",
                    e.getMessage()
            );
        }
    }

    /**
     * Get guest-available channels
     */
    @GetMapping("/guest")
    ResponseEntity<TvBootHttpResponse> getGuestChannels(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting guest channels - page: {}, size: {}", page, size);

        try {
            Page<TvChannel> channelPage = tvChannelService.getGuestChannels(page, size);
            long total = tvChannelService.countGuestChannels();

            // Convert entities to DTOs with logo URLs
            List<TvChannelResponseDTO> channelDTOs = channelPage.getContent().stream()
                    .map(channelMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guest channels retrieved successfully")
                    .build()
                    .addChannels(channelDTOs)
                    .addPagination(page, size, total)
                    .addData("channelType", "guest");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving guest channels", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving guest channels",
                    e.getMessage()
            );
        }
    }

    /**
     * Get channels by language
     */
    @GetMapping("/language/{language}")
    ResponseEntity<TvBootHttpResponse> getChannelsByLanguage(
            @PathVariable String language,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting channels by language: {} - page: {}, size: {}", language, page, size);

        try {
            Page<TvChannel> channelPage = tvChannelService.getChannelsByLanguage(language, page, size);
            long total = channelPage.getTotalElements();

            // Convert entities to DTOs with logo URLs
            List<TvChannelResponseDTO> channelDTOs = channelPage.getContent().stream()
                    .map(channelMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Channels retrieved successfully for language: " + language)
                    .build()
                    .addChannels(channelDTOs)
                    .addPagination(page, size, total)
                    .addData("language", language);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving channels for language: {}", language, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving channels for language",
                    e.getMessage()
            );
        }
    }

    /**
     * Search channels
     */
    @GetMapping("/search")
    public ResponseEntity<TvBootHttpResponse> searchChannels(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String languageId,
            @RequestParam(required = false) Boolean isActive) {

        log.info("=== SEARCH REQUEST ===");
        log.info("Query: '{}', Page: {}, Size: {}", q, page, size);
        log.info("CategoryId: '{}', LanguageId: '{}', IsActive: {}", categoryId, languageId, isActive);

        String searchQuery = (q != null) ? q.trim() : null;
        if (searchQuery != null && searchQuery.isEmpty()) {
            searchQuery = null;
        }

        try {
            Long categoryIdLong = parseStringToLong(categoryId);
            Long languageIdLong = parseStringToLong(languageId);

            log.info("Parsed - CategoryId: {}, LanguageId: {}", categoryIdLong, languageIdLong);

            Page<TvChannel> result = tvChannelService.getChannels(page, size, searchQuery,
                    categoryIdLong, languageIdLong, isActive);

            log.info("Found {} channels out of {} total", result.getContent().size(), result.getTotalElements());

            List<TvChannelResponseDTO> channelDTOs = result.getContent().stream()
                    .map(channelMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Search completed successfully")
                    .build()
                    .addChannels(channelDTOs)
                    .addPagination(page, size, result.getTotalElements())
                    .addData("searchQuery", searchQuery)
                    .addData("resultsFound", result.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("=== SEARCH ERROR ===");
            log.error("Error searching channels with query: '{}'", q, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error searching channels",
                    e.getMessage()
            );
        }
    }

    /**
     * Méthode utilitaire pour convertir String vers Long de manière sécurisée
     */
    private Long parseStringToLong(String value) {
        if (value == null || value.trim().isEmpty() ||
                "null".equalsIgnoreCase(value.trim())) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid number format: '{}', treating as null", value);
            return null;
        }
    }

    /**
     * Get tvChannel by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> getChannelById(@PathVariable Long id) {
        log.info("Getting tvChannel by id: {}", id);

        try {
            Optional<TvChannel> tvChannel = tvChannelService.getChannelById(id);

            if (tvChannel.isPresent()) {
                // Convert entity to DTO with logo URL
                TvChannelResponseDTO channelDTO = channelMapper.toDTO(tvChannel.get());

                TvBootHttpResponse response = TvBootHttpResponse.success()
                        .message("TvChannel found")
                        .build()
                        .addChannel(channelDTO);

                return ResponseEntity.ok(response);
            } else {
                return TvBootHttpResponse.notFoundResponse("TvChannel not found with id: " + id);
            }

        } catch (Exception e) {
            log.error("Error retrieving tvChannel with id: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving tvChannel",
                    e.getMessage()
            );
        }
    }

    /**
     * Get tvChannel by number
     */
    @GetMapping("/number/{channelNumber}")
    public ResponseEntity<TvBootHttpResponse> getChannelByNumber(@PathVariable Integer channelNumber) {
        log.info("Getting tvChannel by number: {}", channelNumber);

        try {
            Optional<TvChannel> tvChannel = tvChannelService.getChannelByNumber(channelNumber);

            if (tvChannel.isPresent()) {
                // Convert entity to DTO with logo URL
                TvChannelResponseDTO channelDTO = channelMapper.toDTO(tvChannel.get());

                TvBootHttpResponse response = TvBootHttpResponse.success()
                        .message("TvChannel found")
                        .build()
                        .addChannel(channelDTO);

                return ResponseEntity.ok(response);
            } else {
                TvBootHttpResponse response = TvBootHttpResponse.channelNotFound(channelNumber.toString())
                        .build();
                return ResponseEntity.status(404).body(response);
            }

        } catch (Exception e) {
            log.error("Error retrieving tvChannel with number: {}", channelNumber, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving tvChannel",
                    e.getMessage()
            );
        }
    }


    @GetMapping("/stream")
    public List<TvChannelStreamDTO> getStreamTvChannels(
            @RequestParam(name = "categoryId", required = false) Byte categoryId
    ) {
        List<TvChannel> tvChannels;
        tvChannels = tvChannelService.getActiveAvailableChannels();

        return tvChannels.stream().map(channelMapper::toStreamDto).toList();
    }


    /**
     * Create new tvChannel
     */
    @PostMapping
    public ResponseEntity<TvBootHttpResponse> createChannel(@Valid @RequestBody TvChannelCreateDTO creatDTO) {
        log.info("Creating new tvChannel: {}", creatDTO.getName());

        TvChannel createdChannel = tvChannelService.createChannel(creatDTO);

        // Convert entity to DTO with logo URL
        TvChannelResponseDTO channelDTO = channelMapper.toDTO(createdChannel);

        TvBootHttpResponse response = TvBootHttpResponse.created()
                .message("TvChannel created successfully")
                .build()
                .addChannel(channelDTO);

        return ResponseEntity.status(201).body(response);


    }

    @PostMapping(path = "/with-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TvChannelResponseDTO> createChannelWithLogo(
            @RequestPart("channel") @Valid TvChannelCreateDTO createDTO,
            @RequestPart("logo") MultipartFile logoFile) {
        TvChannelResponseDTO created = tvChannelService.createChannelWithLogo(createDTO, logoFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Modifier une chaîne avec logo",
            description = "Met à jour une chaîne TV et éventuellement son logo"
    )
    @PutMapping(path = "/{id}/with-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Auditable(action = "UPDATE_CHANNEL_WITH_LOGO", resource = "TV_CHANNEL", logParams = true)
    public ResponseEntity<TvChannelResponseDTO> updateChannelWithLogo(
            @PathVariable Long id,
            @RequestPart("channel") @Valid TvChannelUpdateDTO updateDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {

        log.info("Received request to update TV channel with logo - ID: {}", id);

            TvChannelResponseDTO updatedChannel = tvChannelService.updateChannelWithLogo(id, updateDTO, logoFile);
            log.info("Successfully updated TV channel with logo: {}", updatedChannel.getName());
            return ResponseEntity.ok(updatedChannel);


    }

    @Operation(summary = "Modifier une chaîne")
    @PutMapping("/{id}")
    @Auditable(action = "UPDATE_CHANNEL", resource = "TV_CHANNEL", logParams = true)
    public ResponseEntity<TvBootHttpResponse> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody TvChannelUpdateDTO updateDTO) {

        log.info("Received request to update TV channel with ID: {}", id);
        TvChannelResponseDTO updatedChannel = tvChannelService.updateChannel(id, updateDTO);
        log.info("Successfully updated TV channel: {}", updatedChannel.getName());

        // Convert entity to DTO with logo URL


        TvBootHttpResponse response = TvBootHttpResponse.success()
                .message("TvChannel updated successfully")
                .build()
                .addChannel(updatedChannel);

        return ResponseEntity.ok(response);


    }

    /**
     * Create channels in bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<TvBootHttpResponse> createChannelsInBulk(@Valid @RequestBody List<TvChannel> channels) {
        log.info("Creating {} channels in bulk", channels.size());

        if (channels.isEmpty()) {
            return TvBootHttpResponse.badRequestResponse("TvChannel list cannot be empty");
        }

        try {
            List<TvChannel> createdChannels = tvChannelService.createChannelsInBulk(channels);

            // Convert entities to DTOs with logo URLs
            List<TvChannelResponseDTO> channelDTOs = createdChannels.stream()
                    .map(channelMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.created()
                    .message("Channels created successfully in bulk")
                    .build()
                    .addChannels(channelDTOs)
                    .addCount(channelDTOs.size());

            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error in bulk creation: {}", e.getMessage());
            return TvBootHttpResponse.badRequestResponse(e.getMessage());

        } catch (Exception e) {
            log.error("Error creating channels in bulk", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error creating channels in bulk",
                    e.getMessage()
            );
        }
    }

    /**
     * Get recently added channels
     */
    @GetMapping("/recent")
    public ResponseEntity<TvBootHttpResponse> getRecentChannels(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {

        log.info("Getting {} recent channels", limit);

        try {
            // Get first page with limit size to get most recent
            Page<TvChannel> channelPage = tvChannelService.getChannels(0, limit);

            // Convert entities to DTOs with logo URLs
            List<TvChannelResponseDTO> channelDTOs = channelPage.getContent().stream()
                    .map(channelMapper::toDTO)
                    .collect(Collectors.toList());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Recent channels retrieved successfully")
                    .build()
                    .addChannels(channelDTOs)
                    .addData("limit", limit)
                    .addCount(channelDTOs.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving recent channels", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving recent channels",
                    e.getMessage()
            );
        }
    }

    // Les autres méthodes restent inchangées car elles ne retournent pas de channels
    // (deleteChannel, getAllCategories, getAllLanguages, updateChannelOrder, promoteChannel, getChannelStatistics)

    /**
     * Delete tvChannel (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> deleteChannel(@PathVariable Long id) {
        log.info("Deleting tvChannel with id: {}", id);


            tvChannelService.deleteChannel(id);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("TvChannel deleted successfully")
                    .build()
                    .addData("deletedChannelId", id);

            return ResponseEntity.ok(response);

    }

    // ... autres méthodes utilitaires inchangées (categories, languages, statistics, etc.)
    @GetMapping("/stats")
    public ResponseEntity<TvBootHttpResponse> getChannelStats() {
        log.info("Getting channel statistics");

        try {
            TvChannelStatsDTO stats = tvChannelService.getChannelStatistics();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Channel statistics retrieved successfully")
                    .build()
                    .addData("total", stats.getTotal())
                    .addData("active", stats.getActive())
                    .addData("inactive", stats.getInactive())
                    .addData("byCategory", stats.getByCategory())
                    .addData("byLanguage", stats.getByLanguage());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving channel statistics", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving channel statistics",
                    e.getMessage()
            );
        }
    }
}
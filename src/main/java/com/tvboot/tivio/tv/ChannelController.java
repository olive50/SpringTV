package com.tvboot.tivio.tv;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*") // Configure as needed for your frontend
public class ChannelController {

    private final ChannelService channelService;

    /**
     * Get all channels with pagination
     */
    @GetMapping
    public ResponseEntity<TvBootHttpResponse> getChannels(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting channels - page: {}, size: {}", page, size);

        try {
            Page<TvChannel> channelPage = channelService.getChannels(page, size);
            long total = channelService.countChannels();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Channels retrieved successfully")
                    .build()
                    .addChannels(channelPage.getContent())
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
    public ResponseEntity<TvBootHttpResponse> getChannelsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting channels by category: {} - page: {}, size: {}", category, page, size);

        try {
            Page<TvChannel> channelPage = channelService.getChannelsByCategory(category, page, size);
            long total = channelService.countChannelsByCategory(category);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Channels retrieved successfully for category: " + category)
                    .build()
                    .addChannels(channelPage.getContent())
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
    public ResponseEntity<TvBootHttpResponse> getGuestChannels(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting guest channels - page: {}, size: {}", page, size);

        try {
            Page<TvChannel> channelPage = channelService.getGuestChannels(page, size);
            long total = channelService.countGuestChannels();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guest channels retrieved successfully")
                    .build()
                    .addChannels(channelPage.getContent())
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
     * Get premium channels
     */
    @GetMapping("/premium")
    public ResponseEntity<TvBootHttpResponse> getPremiumChannels(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting premium channels - page: {}, size: {}", page, size);

        try {
            Page<TvChannel> channelPage = channelService.getPremiumChannels(page, size);
            // Note: You might want to add countPremiumChannels() method to service
            long total = channelPage.getTotalElements();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Premium channels retrieved successfully")
                    .build()
                    .addChannels(channelPage.getContent())
                    .addPagination(page, size, total)
                    .addData("channelType", "premium");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving premium channels", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving premium channels",
                    e.getMessage()
            );
        }
    }

    /**
     * Get channels by language
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<TvBootHttpResponse> getChannelsByLanguage(
            @PathVariable String language,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting channels by language: {} - page: {}, size: {}", language, page, size);

        try {
            Page<TvChannel> channelPage = channelService.getChannelsByLanguage(language, page, size);
            long total = channelPage.getTotalElements();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Channels retrieved successfully for language: " + language)
                    .build()
                    .addChannels(channelPage.getContent())
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
            @RequestParam String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Searching channels with query: '{}' - page: {}, size: {}", q, page, size);

        if (q.trim().isEmpty()) {
            return TvBootHttpResponse.badRequestResponse("Search query cannot be empty");
        }

        try {
            Page<TvChannel> channelPage = channelService.searchChannels(q.trim(), page, size);
            long total = channelService.countSearchChannels(q.trim());

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Search completed successfully")
                    .build()
                    .addChannels(channelPage.getContent())
                    .addPagination(page, size, total)
                    .addData("searchQuery", q.trim())
                    .addData("resultsFound", channelPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error searching channels with query: '{}'", q, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error searching channels",
                    e.getMessage()
            );
        }
    }

    /**
     * Get tvChannel by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> getChannelById(@PathVariable Long id) {
        log.info("Getting tvChannel by id: {}", id);

        try {
            Optional<TvChannel> tvChannel = channelService.getChannelById(id);

            if (tvChannel.isPresent()) {
                TvBootHttpResponse response = TvBootHttpResponse.success()
                        .message("TvChannel found")
                        .build()
                        .addChannel(tvChannel.get());

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
            Optional<TvChannel> tvChannel = channelService.getChannelByNumber(channelNumber);

            if (tvChannel.isPresent()) {
                TvBootHttpResponse response = TvBootHttpResponse.success()
                        .message("TvChannel found")
                        .build()
                        .addChannel(tvChannel.get());

                return ResponseEntity.ok(response);
            } else {
                // ✅ Correction ici
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
    /**
     * Create new tvChannel
     */
    @PostMapping
    public ResponseEntity<TvBootHttpResponse> createChannel(@Valid @RequestBody TvChannel tvChannel) {
        log.info("Creating new tvChannel: {}", tvChannel.getName());

        try {
            TvChannel createdChannel = channelService.createChannel(tvChannel);

            TvBootHttpResponse response = TvBootHttpResponse.created()
                    .message("TvChannel created successfully")
                    .build()
                    .addChannel(createdChannel);

            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating tvChannel: {}", e.getMessage());
            return TvBootHttpResponse.badRequestResponse(e.getMessage());

        } catch (Exception e) {
            log.error("Error creating tvChannel", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error creating tvChannel",
                    e.getMessage()
            );
        }
    }

    /**
     * Update tvChannel
     */
    @PutMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody TvChannel tvChannel) {

        log.info("Updating tvChannel with id: {}", id);

        try {
            TvChannel updatedChannel = channelService.updateChannel(id, tvChannel);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("TvChannel updated successfully")
                    .build()
                    .addChannel(updatedChannel);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validation error updating tvChannel: {}", e.getMessage());
            return TvBootHttpResponse.badRequestResponse(e.getMessage());

        } catch (RuntimeException e) {
            log.warn("TvChannel not found for update: {}", e.getMessage());
            return TvBootHttpResponse.notFoundResponse(e.getMessage());

        } catch (Exception e) {
            log.error("Error updating tvChannel with id: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error updating tvChannel",
                    e.getMessage()
            );
        }
    }

    /**
     * Delete tvChannel (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> deleteChannel(@PathVariable Long id) {
        log.info("Deleting tvChannel with id: {}", id);

        try {
            channelService.deleteChannel(id);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("TvChannel deleted successfully")
                    .build()
                    .addData("deletedChannelId", id);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("TvChannel not found for deletion: {}", e.getMessage());
            return TvBootHttpResponse.notFoundResponse(e.getMessage());

        } catch (Exception e) {
            log.error("Error deleting tvChannel with id: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error deleting tvChannel",
                    e.getMessage()
            );
        }
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    public ResponseEntity<TvBootHttpResponse> getAllCategories() {
        log.info("Getting all tvChannel categories");

        try {
            List<String> categories = channelService.getAllCategories();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Categories retrieved successfully")
                    .build()
                    .addData("categories", categories)
                    .addCount(categories.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving categories", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving categories",
                    e.getMessage()
            );
        }
    }

    /**
     * Get all languages
     */
    @GetMapping("/languages")
    public ResponseEntity<TvBootHttpResponse> getAllLanguages() {
        log.info("Getting all tvChannel languages");

        try {
            List<String> languages = channelService.getAllLanguages();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Languages retrieved successfully")
                    .build()
                    .addData("languages", languages)
                    .addCount(languages.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving languages", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving languages",
                    e.getMessage()
            );
        }
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
            List<TvChannel> createdChannels = channelService.createChannelsInBulk(channels);

            TvBootHttpResponse response = TvBootHttpResponse.created()
                    .message("Channels created successfully in bulk")
                    .build()
                    .addChannels(createdChannels)
                    .addCount(createdChannels.size());

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
     * Update tvChannel sort order
     */
    @PatchMapping("/{id}/order")
    public ResponseEntity<TvBootHttpResponse> updateChannelOrder(
            @PathVariable Long id,
            @RequestParam Integer newOrder) {

        log.info("Updating tvChannel order - id: {}, new order: {}", id, newOrder);

        if (newOrder < 0) {
            return TvBootHttpResponse.badRequestResponse("Sort order cannot be negative");
        }

        try {
            channelService.updateChannelOrder(id, newOrder);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("TvChannel order updated successfully")
                    .build()
                    .addData("channelId", id)
                    .addData("newOrder", newOrder);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("TvChannel not found for order update: {}", e.getMessage());
            return TvBootHttpResponse.notFoundResponse(e.getMessage());

        } catch (Exception e) {
            log.error("Error updating tvChannel order - id: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error updating tvChannel order",
                    e.getMessage()
            );
        }
    }

    /**
     * Get tvChannel statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<TvBootHttpResponse> getChannelStatistics() {
        log.info("Getting tvChannel statistics");

        try {
            long totalChannels = channelService.countChannels();
            long guestChannels = channelService.countGuestChannels();
            List<String> categories = channelService.getAllCategories();
            List<String> languages = channelService.getAllLanguages();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("TvChannel statistics retrieved successfully")
                    .build()
                    .addData("totalChannels", totalChannels)
                    .addData("guestChannels", guestChannels)
                    .addData("premiumChannels", totalChannels - guestChannels)
                    .addData("totalCategories", categories.size())
                    .addData("totalLanguages", languages.size())
                    .addData("categories", categories)
                    .addData("languages", languages);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving tvChannel statistics", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving tvChannel statistics",
                    e.getMessage()
            );
        }
    }

    /**
     * Promote a tvChannel to first position
     */
    @PostMapping("/{id}/promote")
    public ResponseEntity<TvBootHttpResponse> promoteChannel(@PathVariable Long id) {
        log.info("Promoting tvChannel with id: {}", id);

        try {
            // Get all channels and shift orders
            List<TvChannel> allChannels = channelService.getChannels(0, Integer.MAX_VALUE).getContent();

            // Find the tvChannel to promote
            TvChannel channelToPromote = null;
            for (TvChannel ch : allChannels) {
                if (ch.getId().equals(id)) {
                    channelToPromote = ch;
                    break;
                }
            }

            if (channelToPromote == null) {
                return TvBootHttpResponse.notFoundResponse("TvChannel not found with id: " + id);
            }

            // Set promoted tvChannel to order 1, shift others
            channelService.updateChannelOrder(id, 1);

            for (TvChannel ch : allChannels) {
                if (!ch.getId().equals(id)) {
                    channelService.updateChannelOrder(ch.getId(), ch.getSortOrder() + 1);
                }
            }

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("TvChannel promoted successfully")
                    .build()
                    .addData("promotedChannelId", id)
                    .addData("newOrder", 1);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error promoting tvChannel with id: {}", id, e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error promoting tvChannel",
                    e.getMessage()
            );
        }
    }

    /**
     * Get channels by HD status
     */
    @GetMapping("/hd")
    public ResponseEntity<TvBootHttpResponse> getHDChannels(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.info("Getting HD channels - page: {}, size: {}", page, size);

        try {
            // Assuming you have this method in service, if not, you can filter in repository
            Page<TvChannel> channelPage = channelService.getChannels(page, size);
            List<TvChannel> hdChannels = channelPage.getContent().stream()
                    .filter(TvChannel::getIsHD)
                    .toList();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("HD channels retrieved successfully")
                    .build()
                    .addChannels(hdChannels)
                    .addData("channelType", "hd")
                    .addCount(hdChannels.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving HD channels", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving HD channels",
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
            Page<TvChannel> channelPage = channelService.getChannels(0, limit);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Recent channels retrieved successfully")
                    .build()
                    .addChannels(channelPage.getContent())
                    .addData("limit", limit)
                    .addCount(channelPage.getContent().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving recent channels", e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Error retrieving recent channels",
                    e.getMessage()
            );
        }
    }
}
package com.tvboot.tivio.guest;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
import com.tvboot.tivio.guest.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/guests")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class GuestController {

    private final GuestService guestService;
    private final GuestMapper guestMapper;

    @GetMapping("/list")
    public ResponseEntity<TvBootHttpResponse> getAllGuests() {
        try {
            log.info("Fetching all guests");
            List<Guest> guests = guestService.getAllGuests();
            List<GuestSummaryDto> guestDtos = guestMapper.toSummaryDtoList(guests);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guests retrieved successfully")
                    .build()
                    .addData("guests", guestDtos)
                    .addCount(guestDtos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching all guests: {}", e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve guests",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/paged")
    public ResponseEntity<TvBootHttpResponse> getAllGuestsPaged(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            log.info("Fetching paged guests - page: {}, size: {}, sortBy: {}, sortDir: {}",
                    page, size, sortBy, sortDir);

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Guest> guestPage = guestService.getAllGuests(pageable);
            Page<GuestSummaryDto> guestDtoPage = guestMapper.toSummaryDtoPage(guestPage);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Paged guests retrieved successfully")
                    .build()
                    .addData("guests", guestDtoPage.getContent())
                    .addPagination(page, size, guestDtoPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching paged guests: {}", e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve paged guests",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> getGuestById(@PathVariable Long id) {
        try {
            log.info("Fetching guest by ID: {}", id);
            return guestService.getGuestById(id)
                    .map(guest -> {
                        GuestResponseDto guestDto = guestMapper.toResponseDto(guest);
                        TvBootHttpResponse response = TvBootHttpResponse.success()
                                .message("Guest retrieved successfully")
                                .build()
                                .addData("guest", guestDto);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(TvBootHttpResponse.notFoundResponse("Guest not found with ID: " + id));
        } catch (Exception e) {
            log.error("Error fetching guest by ID {}: {}", id, e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve guest",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/pms-id/{pmsGuestId}")
    public ResponseEntity<TvBootHttpResponse> getGuestByPmsId(@PathVariable String pmsGuestId) {
        try {
            log.info("Fetching guest by PMS ID: {}", pmsGuestId);
            return guestService.getGuestByGuestId(pmsGuestId)
                    .map(guest -> {
                        GuestResponseDto guestDto = guestMapper.toResponseDto(guest);
                        TvBootHttpResponse response = TvBootHttpResponse.success()
                                .message("Guest retrieved successfully")
                                .build()
                                .addData("guest", guestDto);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(TvBootHttpResponse.notFoundResponse("Guest not found with PMS ID: " + pmsGuestId));
        } catch (Exception e) {
            log.error("Error fetching guest by PMS ID {}: {}", pmsGuestId, e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve guest",
                    e.getMessage()
            );
        }
    }

    @PostMapping("/search")
    public ResponseEntity<TvBootHttpResponse> searchGuests(@Valid @RequestBody GuestSearchDto searchDto) {
        try {
            log.info("Searching guests with criteria: {}", searchDto);

            Sort sort = searchDto.getSortDirection().equalsIgnoreCase("desc") ?
                    Sort.by(searchDto.getSortBy()).descending() :
                    Sort.by(searchDto.getSortBy()).ascending();

            Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize(), sort);

            Page<Guest> guestPage;
            if (searchDto.getSearchTerm() != null && !searchDto.getSearchTerm().trim().isEmpty()) {
                guestPage = guestService.searchGuests(searchDto.getSearchTerm(), pageable);
            } else {
                guestPage = guestService.getAllGuests(pageable);
            }

            Page<GuestSummaryDto> guestDtoPage = guestMapper.toSummaryDtoPage(guestPage);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guest search completed successfully")
                    .build()
                    .addData("guests", guestDtoPage.getContent())
                    .addPagination(searchDto.getPage(), searchDto.getSize(), guestDtoPage.getTotalElements())
                    .addData("searchCriteria", searchDto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching guests: {}", e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to search guests",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/vip")
    public ResponseEntity<TvBootHttpResponse> getVipGuests() {
        try {
            log.info("Fetching VIP guests");
            List<Guest> vipGuests = guestService.getVipGuests();
            List<GuestSummaryDto> vipGuestDtos = guestMapper.toSummaryDtoList(vipGuests);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("VIP guests retrieved successfully")
                    .build()
                    .addData("guests", vipGuestDtos)
                    .addCount(vipGuestDtos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching VIP guests: {}", e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve VIP guests",
                    e.getMessage()
            );
        }
    }

    @PostMapping
    public ResponseEntity<TvBootHttpResponse> createGuest(
            @Valid @RequestBody GuestCreateDto createDto,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                return TvBootHttpResponse.validationErrorResponse("Validation failed", errors);
            }

            log.info("Creating new guest: {} {}", createDto.getFirstName(), createDto.getLastName());
            Guest guest = guestMapper.toEntity(createDto);
            Guest createdGuest = guestService.createGuest(guest);
            GuestResponseDto guestDto = guestMapper.toResponseDto(createdGuest);

            TvBootHttpResponse response = TvBootHttpResponse.created()
                    .message("Guest created successfully")
                    .build()
                    .addData("guest", guestDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Business error creating guest: {}", e.getMessage(), e);
            return TvBootHttpResponse.badRequestResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating guest: {}", e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to create guest",
                    e.getMessage()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> updateGuest(
            @PathVariable Long id,
            @Valid @RequestBody GuestUpdateDto updateDto,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                return TvBootHttpResponse.validationErrorResponse("Validation failed", errors);
            }

            log.info("Updating guest ID: {}", id);
            Guest updatedGuest = guestService.updateGuest(id, updateDto, guestMapper);
            GuestResponseDto guestDto = guestMapper.toResponseDto(updatedGuest);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guest updated successfully")
                    .build()
                    .addData("guest", guestDto);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Business error updating guest {}: {}", id, e.getMessage(), e);
            if (e.getMessage().contains("not found")) {
                return TvBootHttpResponse.notFoundResponse(e.getMessage());
            }
            return TvBootHttpResponse.badRequestResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating guest {}: {}", id, e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to update guest",
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> deleteGuest(@PathVariable Long id) {
        try {
            log.info("Deleting guest ID: {}", id);
            guestService.deleteGuest(id);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guest deleted successfully")
                    .build()
                    .addData("deletedGuestId", id);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Business error deleting guest {}: {}", id, e.getMessage(), e);
            return TvBootHttpResponse.notFoundResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting guest {}: {}", id, e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to delete guest",
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/vip-status")
    public ResponseEntity<TvBootHttpResponse> updateVipStatus(
            @PathVariable Long id,
            @RequestParam Boolean vipStatus) {
        try {
            log.info("Updating VIP status for guest ID: {} to {}", id, vipStatus);
            Guest updatedGuest = guestService.updateVipStatus(id, vipStatus);
            GuestSummaryDto guestDto = guestMapper.toSummaryDto(updatedGuest);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guest VIP status updated successfully")
                    .build()
                    .addData("guest", guestDto);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating VIP status for guest {}: {}", id, e.getMessage(), e);
            return TvBootHttpResponse.notFoundResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating VIP status for guest {}: {}", id, e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to update VIP status",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<TvBootHttpResponse> getGuestStatistics() {
        try {
            log.info("Fetching guest statistics");
            var statistics = guestService.getGuestStatistics();

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guest statistics retrieved successfully")
                    .build()
                    .addData("statistics", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching guest statistics: {}", e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve statistics",
                    e.getMessage()
            );
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<TvBootHttpResponse> getGuestsByRoom(@PathVariable Long roomId) {
        try {
            log.info("Fetching guests for room ID: {}", roomId);
            List<Guest> guests = guestService.getGuestsByRoom(roomId);
            List<GuestSummaryDto> guestDtos = guestMapper.toSummaryDtoList(guests);

            TvBootHttpResponse response = TvBootHttpResponse.success()
                    .message("Guests for room retrieved successfully")
                    .build()
                    .addData("guests", guestDtos)
                    .addData("roomId", roomId)
                    .addCount(guestDtos.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching guests for room {}: {}", roomId, e.getMessage(), e);
            return TvBootHttpResponse.internalServerErrorResponse(
                    "Failed to retrieve guests for room",
                    e.getMessage()
            );
        }
    }
}
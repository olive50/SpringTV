package com.tvboot.tivio.hotel.checkin;



import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
import com.tvboot.tivio.hotel.checkin.dto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "🏨 Check-In/Check-Out", description = "Simple check-in/check-out operations")
public class CheckInCheckOutController {

    private final CheckInCheckOutService checkInCheckOutService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER')")
    @Operation(
            summary = "Check-in a guest",
            description = "Assign a guest to a room and mark as checked-in"
    )
    public ResponseEntity<TvBootHttpResponse> checkIn(
            @Valid @RequestBody CheckInRequestDTO request) {

        log.info("Check-in request: guest={}, room={}",
                request.getGuestId(), request.getRoomId());

        try {
            CheckInResponseDTO response = checkInCheckOutService.checkIn(request);

            TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                    .message("Guest checked in successfully")
                    .build()
                    .addData("checkin", response);

            return ResponseEntity.ok(httpResponse);

        } catch (Exception e) {
            log.error("Check-in failed", e);
            return TvBootHttpResponse.badRequestResponse(e.getMessage());
        }
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER')")
    @Operation(
            summary = "Check-out a guest",
            description = "Remove guest from room and mark as checked-out"
    )
    public ResponseEntity<TvBootHttpResponse> checkOut(
            @Valid @RequestBody CheckOutRequestDTO request) {

        log.info("Check-out request: guest={}", request.getGuestId());

        try {
            CheckInResponseDTO response = checkInCheckOutService.checkOut(request);

            TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                    .message("Guest checked out successfully")
                    .build()
                    .addData("checkout", response);

            return ResponseEntity.ok(httpResponse);

        } catch (Exception e) {
            log.error("Check-out failed", e);
            return TvBootHttpResponse.badRequestResponse(e.getMessage());
        }
    }

    @PostMapping("/change-room")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER')")
    @Operation(
            summary = "Change guest room",
            description = "Move checked-in guest to a different room"
    )
    public ResponseEntity<TvBootHttpResponse> changeRoom(
            @RequestParam Long guestId,
            @RequestParam Long newRoomId,
            @RequestParam(required = false) Long performedBy) {

        log.info("Room change request: guest={}, newRoom={}", guestId, newRoomId);

        try {
            CheckInResponseDTO response = checkInCheckOutService.changeRoom(
                    guestId, newRoomId, performedBy);

            TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                    .message("Room changed successfully")
                    .build()
                    .addData("roomChange", response);

            return ResponseEntity.ok(httpResponse);

        } catch (Exception e) {
            log.error("Room change failed", e);
            return TvBootHttpResponse.badRequestResponse(e.getMessage());
        }
    }

    /**
     * PUBLIC ENDPOINT FOR TV CLIENTS - NO AUTHENTICATION REQUIRED
     */
    @GetMapping("/room-status/{roomNumber}")
    @Operation(
            summary = "Get room status for TV client",
            description = "Check if room is occupied and get guest language (PUBLIC endpoint for TV)"
    )
    public ResponseEntity<TvBootHttpResponse> getRoomStatus(
            @PathVariable String roomNumber) {

        log.debug("Room status request for room: {}", roomNumber);

        try {
            RoomStatusDTO response = checkInCheckOutService.getRoomStatus(roomNumber);

            TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                    .message("Room status retrieved")
                    .build()
                    .addData("roomStatus", response);

            return ResponseEntity.ok(httpResponse);

        } catch (Exception e) {
            log.error("Failed to get room status for room: {}", roomNumber, e);
            return TvBootHttpResponse.notFoundResponse("Room not found: " + roomNumber);
        }
    }

    @GetMapping("/history/guest/{guestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MANAGER')")
    @Operation(summary = "Get check-in history for guest")
    public ResponseEntity<TvBootHttpResponse> getGuestHistory(@PathVariable Long guestId) {
        // Implementation...
        return ResponseEntity.ok(TvBootHttpResponse.success().message("History").build());
    }
}
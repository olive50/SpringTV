package com.tvboot.tivio.room;

import com.tvboot.tivio.room.dto.RoomRequest;
import com.tvboot.tivio.room.dto.RoomResponse;
import com.tvboot.tivio.room.dto.RoomStatsDTO;
import com.tvboot.tivio.room.dto.RoomSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        RoomResponse response = roomService.createRoom(roomRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{roomNumber}")
    public ResponseEntity<RoomResponse> getRoomByNumber(@PathVariable String roomNumber) {
        RoomResponse response = roomService.getRoomByNumber(roomNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getAllRooms(
            @PageableDefault(size = 5) Pageable pageable) {
        Page<RoomResponse> response = roomService.getAllRooms(pageable);
        return ResponseEntity.ok(response);
    }

   
    @GetMapping("/available")
    public ResponseEntity<List<RoomSummary>> getAvailableRooms() {
        List<RoomSummary> response = roomService.getAvailableRooms();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<RoomStatsDTO> getRoomStats() {
        RoomStatsDTO stats = roomService.getRoomStatistics();
        return ResponseEntity.ok(stats);
    }


    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest roomRequest) {
        RoomResponse response = roomService.updateRoom(id, roomRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RoomResponse> updateRoomStatus(
            @PathVariable Long id,
            @RequestParam Room.RoomStatus status) {
        RoomResponse response = roomService.updateRoomStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoomSummary>> searchRooms(@RequestParam String q) {
        List<RoomSummary> response = roomService.searchRooms(q);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{roomType}")
    public ResponseEntity<List<RoomSummary>> getRoomsByType(@PathVariable Room.RoomType roomType) {
        List<RoomSummary> response = roomService.getRoomsByType(roomType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/building/{building}")
    public ResponseEntity<List<RoomSummary>> getRoomsByBuilding(@PathVariable String building) {
        List<RoomSummary> response = roomService.getRoomsByBuilding(building);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<RoomSummary>> getRoomsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<RoomSummary> response = roomService.getRoomsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/capacity/{guests}")
    public ResponseEntity<List<RoomSummary>> getRoomsAvailableForGuests(@PathVariable int guests) {
        List<RoomSummary> response = roomService.getRoomsAvailableForGuests(guests);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/available")
    public ResponseEntity<Long> getAvailableRoomsCount() {
        long count = roomService.getAvailableRoomsCount();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{roomId}/channel-package/{packageId}")
    public ResponseEntity<RoomResponse> assignChannelPackage(
            @PathVariable Long roomId,
            @PathVariable Long packageId) {
        RoomResponse response = roomService.assignChannelPackage(roomId, packageId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roomId}/channel-package")
    public ResponseEntity<RoomResponse> removeChannelPackage(@PathVariable Long roomId) {
        RoomResponse response = roomService.removeChannelPackage(roomId);
        return ResponseEntity.ok(response);
    }
}
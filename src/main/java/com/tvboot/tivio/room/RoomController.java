package com.tvboot.tivio.room;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
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
    public ResponseEntity<TvBootHttpResponse> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        RoomResponse response = roomService.createRoom(roomRequest);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.created()
                .message("Room created successfully")
                .build()
                .addRoom(response);
        return ResponseEntity.status(201).body(httpResponse);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> getRoomById(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Room retrieved successfully")
                .build()
                .addRoom(response);
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/number/{roomNumber}")
    public ResponseEntity<TvBootHttpResponse> getRoomByNumber(@PathVariable String roomNumber) {
        RoomResponse response = roomService.getRoomByNumber(roomNumber);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Room retrieved successfully")
                .build()
                .addRoom(response);
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping
    public ResponseEntity<TvBootHttpResponse> getAllRooms(
            @PageableDefault(size = 5) Pageable pageable) {
        Page<RoomResponse> responsePage = roomService.getAllRooms(pageable);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Rooms retrieved successfully")
                .build()
                .addData("rooms", responsePage.getContent())
                .addPagination(
                        responsePage.getNumber(),
                        responsePage.getSize(),
                        responsePage.getTotalElements()
                );
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/available")
    public ResponseEntity<TvBootHttpResponse> getAvailableRooms() {
        List<RoomSummary> rooms = roomService.getAvailableRooms();
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Available rooms retrieved successfully")
                .build()
                .addData("rooms", rooms)
                .addCount(rooms.size());
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/statistics")
    public ResponseEntity<TvBootHttpResponse> getRoomStats() {
        RoomStatsDTO stats = roomService.getRoomStatistics();
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Room statistics retrieved successfully")
                .build()
                .addData("statistics", stats);
        return ResponseEntity.ok(httpResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest roomRequest) {
        RoomResponse response = roomService.updateRoom(id, roomRequest);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Room updated successfully")
                .build()
                .addRoom(response);
        return ResponseEntity.ok(httpResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TvBootHttpResponse> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Room deleted successfully")
                .build();
        return ResponseEntity.ok(httpResponse);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TvBootHttpResponse> updateRoomStatus(
            @PathVariable Long id,
            @RequestParam Room.RoomStatus status) {
        RoomResponse response = roomService.updateRoomStatus(id, status);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Room status updated successfully")
                .build()
                .addRoom(response);
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<TvBootHttpResponse> searchRooms(@RequestParam String q) {
        List<RoomSummary> rooms = roomService.searchRooms(q);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Room search completed successfully")
                .build()
                .addData("rooms", rooms)
                .addCount(rooms.size())
                .addData("searchTerm", q);
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/type/{roomType}")
    public ResponseEntity<TvBootHttpResponse> getRoomsByType(@PathVariable Room.RoomType roomType) {
        List<RoomSummary> rooms = roomService.getRoomsByType(roomType);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Rooms retrieved by type successfully")
                .build()
                .addData("rooms", rooms)
                .addCount(rooms.size())
                .addData("roomType", roomType);
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/building/{building}")
    public ResponseEntity<TvBootHttpResponse> getRoomsByBuilding(@PathVariable String building) {
        List<RoomSummary> rooms = roomService.getRoomsByBuilding(building);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Rooms retrieved by building successfully")
                .build()
                .addData("rooms", rooms)
                .addCount(rooms.size())
                .addData("building", building);
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/price-range")
    public ResponseEntity<TvBootHttpResponse> getRoomsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<RoomSummary> rooms = roomService.getRoomsByPriceRange(minPrice, maxPrice);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Rooms retrieved by price range successfully")
                .build()
                .addData("rooms", rooms)
                .addCount(rooms.size())
                .addData("priceRange", new Object() {
                    public final BigDecimal min = minPrice;
                    public final BigDecimal max = maxPrice;
                });
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/capacity/{guests}")
    public ResponseEntity<TvBootHttpResponse> getRoomsAvailableForGuests(@PathVariable int guests) {
        List<RoomSummary> rooms = roomService.getRoomsAvailableForGuests(guests);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Rooms available for guests retrieved successfully")
                .build()
                .addData("rooms", rooms)
                .addCount(rooms.size())
                .addData("guestCapacity", guests);
        return ResponseEntity.ok(httpResponse);
    }

    @GetMapping("/count/available")
    public ResponseEntity<TvBootHttpResponse> getAvailableRoomsCount() {
        long count = roomService.getAvailableRoomsCount();
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Available rooms count retrieved successfully")
                .build()
                .addCount(count);
        return ResponseEntity.ok(httpResponse);
    }

    @PostMapping("/{roomId}/channel-package/{packageId}")
    public ResponseEntity<TvBootHttpResponse> assignChannelPackage(
            @PathVariable Long roomId,
            @PathVariable Long packageId) {
        RoomResponse response = roomService.assignChannelPackage(roomId, packageId);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Channel package assigned to room successfully")
                .build()
                .addRoom(response)
                .addData("packageId", packageId);
        return ResponseEntity.ok(httpResponse);
    }

    @DeleteMapping("/{roomId}/channel-package")
    public ResponseEntity<TvBootHttpResponse> removeChannelPackage(@PathVariable Long roomId) {
        RoomResponse response = roomService.removeChannelPackage(roomId);
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Channel package removed from room successfully")
                .build()
                .addRoom(response);
        return ResponseEntity.ok(httpResponse);
    }
}
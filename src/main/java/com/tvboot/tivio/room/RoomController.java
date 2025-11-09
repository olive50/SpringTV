package com.tvboot.tivio.room;

import com.tvboot.tivio.common.dto.respone.TvBootHttpResponse;
import com.tvboot.tivio.room.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/rooms")
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
            @PageableDefault(
                    page = 0,
                    size = 23,
                    sort = {"floorNumber", "roomNumber"},
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
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


    @GetMapping("/count/available")
    public ResponseEntity<TvBootHttpResponse> getAvailableRoomsCount() {
        long count = roomService.getAvailableRoomsCount();
        TvBootHttpResponse httpResponse = TvBootHttpResponse.success()
                .message("Available rooms count retrieved successfully")
                .build()
                .addCount(count);
        return ResponseEntity.ok(httpResponse);
    }

    // ✅ POST /rooms/{roomId}/guests  → check-in
    @PostMapping("/{roomNumber}/checking")
    public ResponseEntity<?> checkIn(@PathVariable String roomNumber, @RequestBody GuestRoomDto dto) {
        var room = roomService.checkIn(roomNumber, dto);
        return ResponseEntity.ok(room);
    }

    // ✅ PUT /rooms/{roomId}/guests  → check-out
    @PutMapping("/{roomNumber}/checkout")
    public ResponseEntity<?> checkOut(@PathVariable String roomNumber) {
        var room = roomService.checkOut(roomNumber);
        return ResponseEntity.ok(room);
    }

}
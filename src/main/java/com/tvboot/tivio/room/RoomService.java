package com.tvboot.tivio.room;

import com.tvboot.tivio.room.dto.RoomRequest;
import com.tvboot.tivio.room.dto.RoomResponse;
import com.tvboot.tivio.room.dto.RoomSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface RoomService {

    RoomResponse createRoom(RoomRequest roomRequest);

    RoomResponse getRoomById(Long id);

    RoomResponse getRoomByNumber(String roomNumber);

    Page<RoomResponse> getAllRooms(Pageable pageable);

    List<RoomSummary> getAvailableRooms();

    RoomResponse updateRoom(Long id, RoomRequest roomRequest);

    void deleteRoom(Long id);

    RoomResponse updateRoomStatus(Long id, Room.RoomStatus status);

    List<RoomSummary> searchRooms(String searchTerm);

    List<RoomSummary> getRoomsByType(Room.RoomType roomType);

    List<RoomSummary> getRoomsByBuilding(String building);

    List<RoomSummary> getRoomsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<RoomSummary> getRoomsAvailableForGuests(int numberOfGuests);

    long getAvailableRoomsCount();

    RoomResponse assignChannelPackage(Long roomId, Long channelPackageId);

    RoomResponse removeChannelPackage(Long roomId);
}
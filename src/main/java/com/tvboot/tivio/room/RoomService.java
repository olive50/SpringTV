package com.tvboot.tivio.room;

import com.tvboot.tivio.room.dto.*;
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

    List<RoomSummary> searchRooms(String searchTerm);

    List<RoomSummary> getRoomsByType(Room.RoomType roomType);

    List<RoomSummary> getRoomsAvailableForGuests(int numberOfGuests);

    long getAvailableRoomsCount();

    RoomResponse assignChannelPackage(Long roomId, Long channelPackageId);

    RoomResponse removeChannelPackage(Long roomId);
    RoomStatsDTO getRoomStatistics();

    Object checkIn(String roomNumber, GuestRoomDto dto);

    Object checkOut(String roomNumber);

    List<RoomSummary> getRoomsByFloorNumber(Integer floorNumber);
}
package com.tvboot.tivio.service;

import com.tvboot.tivio.entities.Room;
import com.tvboot.tivio.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Page<Room> getAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public Optional<Room> getRoomByNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }

    public List<Room> getRoomsByType(Room.RoomType roomType) {
        return roomRepository.findByRoomType(roomType);
    }

    public List<Room> getRoomsByStatus(Room.RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    public List<Room> getAvailableRoomsByType(Room.RoomType roomType) {
        return roomRepository.findAvailableRoomsByType(roomType);
    }

    public List<Room> getRoomsByFloor(Integer floorNumber) {
        return roomRepository.findByFloorNumber(floorNumber);
    }

    public Room createRoom(Room room) {
        if (roomRepository.findByRoomNumber(room.getRoomNumber()).isPresent()) {
            throw new RuntimeException("Room number already exists: " + room.getRoomNumber());
        }

        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found: " + id));

        // Check unique room number if being updated
        if (!room.getRoomNumber().equals(roomDetails.getRoomNumber())) {
            if (roomRepository.findByRoomNumber(roomDetails.getRoomNumber()).isPresent()) {
                throw new RuntimeException("Room number already exists: " + roomDetails.getRoomNumber());
            }
        }

        // Update fields
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setRoomType(roomDetails.getRoomType());
        room.setFloorNumber(roomDetails.getFloorNumber());
        room.setBuilding(roomDetails.getBuilding());
        room.setMaxOccupancy(roomDetails.getMaxOccupancy());
        room.setPricePerNight(roomDetails.getPricePerNight());
        room.setStatus(roomDetails.getStatus());
        room.setDescription(roomDetails.getDescription());
        room.setAmenities(roomDetails.getAmenities());

        if (roomDetails.getChannelPackage() != null) {
            room.setChannelPackage(roomDetails.getChannelPackage());
        }

        return roomRepository.save(room);
    }

    public Room updateRoomStatus(Long id, Room.RoomStatus status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found: " + id));

        room.setStatus(status);
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found: " + id);
        }
        roomRepository.deleteById(id);
    }
}
package com.tvboot.tivio.room;


import com.tvboot.tivio.exception.ResourceNotFoundException;
import com.tvboot.tivio.room.dto.RoomRequest;
import com.tvboot.tivio.room.dto.RoomResponse;
import com.tvboot.tivio.room.dto.RoomSummary;
import com.tvboot.tivio.room.exception.RoomAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    public RoomResponse createRoom(RoomRequest roomRequest) {
        log.info("Creating new room with number: {}", roomRequest.getRoomNumber());

        if (roomRepository.existsByRoomNumber(roomRequest.getRoomNumber())) {
            throw new RoomAlreadyExistsException("Room with number " + roomRequest.getRoomNumber() + " already exists");
        }

        Room room = roomMapper.toEntity(roomRequest);
        Room savedRoom = roomRepository.save(room);

        log.info("Room created successfully with ID: {}", savedRoom.getId());
        return roomMapper.toResponse(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        log.debug("Fetching room with ID: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomByNumber(String roomNumber) {
        log.debug("Fetching room with number: {}", roomNumber);
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with number: " + roomNumber));
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> getAllRooms(Pageable pageable) {
        log.debug("Fetching all rooms with pagination: {}", pageable);
        return roomRepository.findAll(pageable)
                .map(roomMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSummary> getAvailableRooms() {
        log.debug("Fetching all available rooms");
        return roomRepository.findByStatus(Room.RoomStatus.AVAILABLE)
                .stream()
                .map(roomMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse updateRoom(Long id, RoomRequest roomRequest) {
        log.info("Updating room with ID: {}", id);

        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        // Check if room number is being changed and if it already exists
        if (!existingRoom.getRoomNumber().equals(roomRequest.getRoomNumber()) &&
                roomRepository.existsByRoomNumber(roomRequest.getRoomNumber())) {
            throw new RoomAlreadyExistsException("Room with number " + roomRequest.getRoomNumber() + " already exists");
        }

        roomMapper.updateEntityFromRequest(roomRequest, existingRoom);
        Room updatedRoom = roomRepository.save(existingRoom);

        log.info("Room updated successfully with ID: {}", id);
        return roomMapper.toResponse(updatedRoom);
    }

    @Override
    public void deleteRoom(Long id) {
        log.info("Deleting room with ID: {}", id);

        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with ID: " + id);
        }

        roomRepository.deleteById(id);
        log.info("Room deleted successfully with ID: {}", id);
    }

    @Override
    public RoomResponse updateRoomStatus(Long id, Room.RoomStatus status) {
        log.info("Updating room status to {} for room ID: {}", status, id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        room.setStatus(status);
        Room updatedRoom = roomRepository.save(room);

        log.info("Room status updated successfully for ID: {}", id);
        return roomMapper.toResponse(updatedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSummary> searchRooms(String searchTerm) {
        log.debug("Searching rooms with term: {}", searchTerm);
        return roomRepository.searchRooms(searchTerm)
                .stream()
                .map(roomMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSummary> getRoomsByType(Room.RoomType roomType) {
        log.debug("Fetching rooms by type: {}", roomType);
        return roomRepository.findByRoomType(roomType)
                .stream()
                .map(roomMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSummary> getRoomsByBuilding(String building) {
        log.debug("Fetching rooms in building: {}", building);
        return roomRepository.findByBuilding(building)
                .stream()
                .map(roomMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSummary> getRoomsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Fetching rooms in price range: {} - {}", minPrice, maxPrice);
        return roomRepository.findAvailableRoomsByPriceRange(minPrice, maxPrice)
                .stream()
                .map(roomMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSummary> getRoomsAvailableForGuests(int numberOfGuests) {
        log.debug("Fetching rooms available for {} guests", numberOfGuests);
        return roomRepository.findAvailableRoomsForGuests(numberOfGuests)
                .stream()
                .map(roomMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getAvailableRoomsCount() {
        log.debug("Counting available rooms");
        return roomRepository.countAvailableRooms();
    }

    @Override
    public RoomResponse assignChannelPackage(Long roomId, Long channelPackageId) {
        log.info("Assigning channel package {} to room {}", channelPackageId, roomId);
        // Implementation would depend on your ChannelPackage entity
        // This is a placeholder implementation
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        // room.setChannelPackage(channelPackage); // Uncomment when ChannelPackage is implemented
        Room updatedRoom = roomRepository.save(room);
        return roomMapper.toResponse(updatedRoom);
    }

    @Override
    public RoomResponse removeChannelPackage(Long roomId) {
        log.info("Removing channel package from room {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        // room.setChannelPackage(null); // Uncomment when ChannelPackage is implemented
        Room updatedRoom = roomRepository.save(room);
        return roomMapper.toResponse(updatedRoom);
    }
}
package com.tvboot.tivio.hotel.checkin;


import com.tvboot.tivio.auth.UserRepository;
import com.tvboot.tivio.common.enumeration.CheckinStatus;
import com.tvboot.tivio.common.exception.BusinessException;
import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.guest.Guest;
import com.tvboot.tivio.guest.GuestRepository;
import com.tvboot.tivio.hotel.checkin.dto.*;

import com.tvboot.tivio.room.Room;
import com.tvboot.tivio.room.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CheckInCheckOutService {

    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final CheckinHistoryRepository checkinHistoryRepository;
    private final UserRepository userRepository;

    /**
     * Perform simple check-in: assign guest to room
     */
    public CheckInResponseDTO checkIn(CheckInRequestDTO request) {
        log.info("Starting check-in for guest {} to room {}",
                request.getGuestId(), request.getRoomId());

        // 1. Load guest
        Guest guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Guest", "id", request.getGuestId()));

        // 2. Verify guest can check-in
        if (!guest.canCheckIn()) {
            throw new BusinessException(
                    "Guest is already checked in to room: " + guest.getRoom().getRoomNumber()
            );
        }

        // 3. Load room
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room", "id", request.getRoomId()));

        // 4. Verify room is available
        if (room.getStatus() == Room.RoomStatus.OCCUPIED) {
            throw new BusinessException(
                    "Room " + room.getRoomNumber() + " is already occupied"
            );
        }

        // 5. Perform check-in
        LocalDateTime now = LocalDateTime.now();

        // Update guest
        guest.setRoom(room);
        guest.setCheckinStatus(CheckinStatus.CHECKED_IN);
        guest.setCheckinTime(now);
        guest.setCheckoutTime(null);

        // Update room
        room.setStatus(Room.RoomStatus.OCCUPIED);

        // Save changes
        guestRepository.save(guest);
        roomRepository.save(room);

        // 6. Log history
        saveCheckinHistory(guest, room, "CHECK_IN", request.getPerformedBy(), request.getNotes());

        log.info("Check-in completed: Guest {} checked into room {}",
                guest.getPmsGuestId(), room.getRoomNumber());

        // 7. Return response
        return CheckInResponseDTO.builder()
                .guestId(guest.getId())
                .guestName(guest.getFirstName() + " " + guest.getLastName())
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .status("SUCCESS")
                .checkinTime(now)
                .message("Check-in completed successfully")
                .build();
    }

    /**
     * Perform simple check-out: remove guest from room
     */
    public CheckInResponseDTO checkOut(CheckOutRequestDTO request) {
        log.info("Starting check-out for guest {}", request.getGuestId());

        // 1. Load guest
        Guest guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Guest", "id", request.getGuestId()));

        // 2. Verify guest is checked in
        if (!guest.isCheckedIn()) {
            throw new BusinessException("Guest is not checked in");
        }

        Room room = guest.getRoom();
        if (room == null) {
            throw new BusinessException("Guest has no assigned room");
        }

        // 3. Perform check-out
        LocalDateTime now = LocalDateTime.now();

        // Update guest
        guest.setCheckinStatus(CheckinStatus.CHECKED_OUT);
        guest.setCheckoutTime(now);
        guest.setRoom(null); // Remove room assignment

        // Update room
        room.setStatus(Room.RoomStatus.CLEANING); // Or AVAILABLE if no cleaning needed

        // Save changes
        guestRepository.save(guest);
        roomRepository.save(room);

        // 4. Log history
        saveCheckinHistory(guest, room, "CHECK_OUT", request.getPerformedBy(), request.getNotes());

        log.info("Check-out completed: Guest {} checked out from room {}",
                guest.getPmsGuestId(), room.getRoomNumber());

        // 5. Return response
        return CheckInResponseDTO.builder()
                .guestId(guest.getId())
                .guestName(guest.getFirstName() + " " + guest.getLastName())
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .status("SUCCESS")
                .checkinTime(now)
                .message("Check-out completed successfully")
                .build();
    }

    /**
     * Get room status for TV client
     */
    @Transactional(readOnly = true)
    public RoomStatusDTO getRoomStatus(String roomNumber) {
        log.debug("Getting room status for room: {}", roomNumber);

        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room", "roomNumber", roomNumber));

        boolean isOccupied = room.getStatus() == Room.RoomStatus.OCCUPIED;

        RoomStatusDTO response = RoomStatusDTO.builder()
                .roomNumber(roomNumber)
                .occupied(isOccupied)
                .build();

        // If occupied, get guest info
        if (isOccupied) {
            Guest guest = guestRepository.findByRoom(room)
                    .orElse(null);

            if (guest != null && guest.isCheckedIn()) {
                RoomStatusDTO.GuestInfoDTO guestInfo = RoomStatusDTO.GuestInfoDTO.builder()
                        .firstName(guest.getFirstName())
                        .lastName(guest.getLastName())
                        .languageCode(guest.getLanguage() != null ?
                                guest.getLanguage().getIso6391() : "en")
                        .languageName(guest.getLanguage() != null ?
                                guest.getLanguage().getName() : "English")
                        .build();

                response.setGuest(guestInfo);
            }
        }

        return response;
    }

    /**
     * Change room for checked-in guest
     */
    public CheckInResponseDTO changeRoom(Long guestId, Long newRoomId, Long performedBy) {
        log.info("Changing room for guest {} to room {}", guestId, newRoomId);

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));

        if (!guest.isCheckedIn()) {
            throw new BusinessException("Guest is not checked in");
        }

        Room oldRoom = guest.getRoom();
        Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", newRoomId));

        if (newRoom.getStatus() == Room.RoomStatus.OCCUPIED) {
            throw new BusinessException("New room is already occupied");
        }

        // Update old room
        oldRoom.setStatus(Room.RoomStatus.CLEANING);

        // Update new room
        newRoom.setStatus(Room.RoomStatus.OCCUPIED);

        // Update guest
        guest.setRoom(newRoom);

        // Save
        roomRepository.save(oldRoom);
        roomRepository.save(newRoom);
        guestRepository.save(guest);

        // Log
        saveCheckinHistory(guest, newRoom, "ROOM_CHANGE", performedBy,
                "Changed from room " + oldRoom.getRoomNumber());

        log.info("Room changed successfully from {} to {}",
                oldRoom.getRoomNumber(), newRoom.getRoomNumber());

        return CheckInResponseDTO.builder()
                .guestId(guest.getId())
                .guestName(guest.getFirstName() + " " + guest.getLastName())
                .roomId(newRoom.getId())
                .roomNumber(newRoom.getRoomNumber())
                .status("SUCCESS")
                .message("Room changed successfully")
                .build();
    }

    // Helper method to save history
    private void saveCheckinHistory(Guest guest, Room room, String action,
                                    Long performedBy, String notes) {
        CheckinHistory history = CheckinHistory.builder()
                .guest(guest)
                .room(room)
                .action(action)
                .performedBy(performedBy != null ?
                        userRepository.getReferenceById(performedBy) : null)
                .notes(notes)
                .build();

        checkinHistoryRepository.save(history);
    }
}
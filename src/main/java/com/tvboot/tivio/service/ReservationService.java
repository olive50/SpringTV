package com.tvboot.tivio.service;

import com.tvboot.tivio.entities.Reservation;
import com.tvboot.tivio.entities.Room;
import com.tvboot.tivio.repository.ReservationRepository;
import com.tvboot.tivio.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Page<Reservation> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public Optional<Reservation> getReservationByNumber(String reservationNumber) {
        return reservationRepository.findByReservationNumber(reservationNumber);
    }

    public List<Reservation> getReservationsByGuest(Long guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    public List<Reservation> getActiveReservations() {
        return reservationRepository.findActiveReservations(LocalDateTime.now());
    }

    public List<Reservation> getTodayArrivals() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return reservationRepository.findArrivalsForDate(today);
    }

    public List<Reservation> getTodayDepartures() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return reservationRepository.findDeparturesForDate(today);
    }

    public Reservation createReservation(Reservation reservation) {
        // Generate reservation number if not provided
        if (reservation.getReservationNumber() == null) {
            reservation.setReservationNumber(generateReservationNumber());
        }

        // Validate unique reservation number
        if (reservationRepository.findByReservationNumber(reservation.getReservationNumber()).isPresent()) {
            throw new RuntimeException("Reservation number already exists: " + reservation.getReservationNumber());
        }

        // Set default status if not provided
        if (reservation.getStatus() == null) {
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        }

        return reservationRepository.save(reservation);
    }

    public Reservation updateReservation(Long id, Reservation reservationDetails) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));

        // Update fields
        if (reservationDetails.getRoom() != null) {
            reservation.setRoom(reservationDetails.getRoom());
        }
        if (reservationDetails.getCheckInDate() != null) {
            reservation.setCheckInDate(reservationDetails.getCheckInDate());
        }
        if (reservationDetails.getCheckOutDate() != null) {
            reservation.setCheckOutDate(reservationDetails.getCheckOutDate());
        }
        if (reservationDetails.getNumberOfGuests() != null) {
            reservation.setNumberOfGuests(reservationDetails.getNumberOfGuests());
        }
        if (reservationDetails.getTotalAmount() != null) {
            reservation.setTotalAmount(reservationDetails.getTotalAmount());
        }
        if (reservationDetails.getStatus() != null) {
            reservation.setStatus(reservationDetails.getStatus());
        }
        if (reservationDetails.getSpecialRequests() != null) {
            reservation.setSpecialRequests(reservationDetails.getSpecialRequests());
        }
        if (reservationDetails.getNotes() != null) {
            reservation.setNotes(reservationDetails.getNotes());
        }

        return reservationRepository.save(reservation);
    }

    public Reservation checkIn(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Reservation is not in confirmed status");
        }

        reservation.setStatus(Reservation.ReservationStatus.CHECKED_IN);
        reservation.setActualCheckIn(LocalDateTime.now());

        // Update room status to occupied
        Room room = reservation.getRoom();
        room.setStatus(Room.RoomStatus.OCCUPIED);
        roomRepository.save(room);

        return reservationRepository.save(reservation);
    }

    public Reservation checkOut(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            throw new RuntimeException("Reservation is not checked in");
        }

        reservation.setStatus(Reservation.ReservationStatus.CHECKED_OUT);
        reservation.setActualCheckOut(LocalDateTime.now());

        // Update room status to cleaning
        Room room = reservation.getRoom();
        room.setStatus(Room.RoomStatus.CLEANING);
        roomRepository.save(room);

        return reservationRepository.save(reservation);
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new RuntimeException("Reservation not found: " + id);
        }
        reservationRepository.deleteById(id);
    }

    private String generateReservationNumber() {
        String prefix = "RES";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String uuid = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return prefix + timestamp + uuid;
    }
}
package com.tvboot.tivio.guest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Reservation>> getAllReservationsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Reservation> reservations = reservationService.getAllReservations(pageable);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id)
                .map(reservation -> ResponseEntity.ok(reservation))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{reservationNumber}")
    public ResponseEntity<Reservation> getReservationByNumber(@PathVariable String reservationNumber) {
        return reservationService.getReservationByNumber(reservationNumber)
                .map(reservation -> ResponseEntity.ok(reservation))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<Reservation>> getReservationsByGuest(@PathVariable Long guestId) {
        List<Reservation> reservations = reservationService.getReservationsByGuest(guestId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Reservation>> getActiveReservations() {
        List<Reservation> reservations = reservationService.getActiveReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/arrivals/today")
    public ResponseEntity<List<Reservation>> getTodayArrivals() {
        List<Reservation> reservations = reservationService.getTodayArrivals();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/departures/today")
    public ResponseEntity<List<Reservation>> getTodayDepartures() {
        List<Reservation> reservations = reservationService.getTodayDepartures();
        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody Reservation reservation) {
        try {
            Reservation createdReservation = reservationService.createReservation(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody Reservation reservationDetails) {
        try {
            Reservation updatedReservation = reservationService.updateReservation(id, reservationDetails);
            return ResponseEntity.ok(updatedReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<Reservation> checkIn(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.checkIn(id);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<Reservation> checkOut(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.checkOut(id);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        try {
            reservationService.deleteReservation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
package com.tvboot.tivio.guest;

import com.tvboot.tivio.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationNumber(String reservationNumber);

    List<Reservation> findByGuestId(Long guestId);
    List<Reservation> findByRoomId(Long roomId);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.checkInDate <= :date AND r.checkOutDate >= :date")
    List<Reservation> findActiveReservations(@Param("date") LocalDateTime date);

    @Query("SELECT r FROM Reservation r WHERE r.checkInDate = :date AND r.status = 'CONFIRMED'")
    List<Reservation> findArrivalsForDate(@Param("date") LocalDateTime date);

    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate = :date AND r.status = 'CHECKED_IN'")
    List<Reservation> findDeparturesForDate(@Param("date") LocalDateTime date);

    @Query("SELECT r FROM Reservation r WHERE r.guest.id = :guestId AND r.status = 'CHECKED_IN'")
    Optional<Reservation> findCurrentReservation(@Param("guestId") Long guestId);
}
package com.tvboot.tivio.hotel.checkin;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CheckinHistoryRepository extends JpaRepository<CheckinHistory, Long> {
    List<CheckinHistory> findByGuestIdOrderByPerformedAtDesc(Long guestId);
    List<CheckinHistory> findByRoomIdOrderByPerformedAtDesc(Long roomId);
}
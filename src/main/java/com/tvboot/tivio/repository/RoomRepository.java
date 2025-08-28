package com.tvboot.tivio.repository;

import com.tvboot.tivio.entities.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);

    List<Room> findByRoomType(Room.RoomType roomType);
    List<Room> findByStatus(Room.RoomStatus status);
    List<Room> findByFloorNumber(Integer floorNumber);

    Page<Room> findByRoomTypeAndStatus(Room.RoomType roomType, Room.RoomStatus status, Pageable pageable);

    @Query("SELECT r FROM Room r WHERE r.building = :building AND r.floorNumber = :floor")
    List<Room> findByBuildingAndFloor(@Param("building") String building, @Param("floor") Integer floor);

    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND r.roomType = :roomType")
    List<Room> findAvailableRoomsByType(@Param("roomType") Room.RoomType roomType);
}
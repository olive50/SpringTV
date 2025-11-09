package com.tvboot.tivio.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Basic CRUD operations
    Optional<Room> findByRoomNumber(String roomNumber);
    boolean existsByRoomNumber(String roomNumber);

    // Find methods

    List<Room> findByRoomType(Room.RoomType roomType);

    List<Room> findByFloorNumber(Integer floorNumber);

    // Complex queries
    @Query("SELECT r FROM Room r WHERE r.occupied = false ")
    List<Room> findAvailableRooms();

        // ============ COUNT METHODS ============

    // Basic count methods
    @Query("SELECT COUNT(r) FROM Room r WHERE r.occupied = false ")
    int countAvailableRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.occupied = true")
    int countOccupiedRooms();

    // Count by room type
    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType = :roomType")
    int countByRoomType(@Param("roomType") Room.RoomType roomType);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType = :roomType AND r.occupied = false ")
    int countAvailableByRoomType(@Param("roomType") Room.RoomType roomType);

    // Count by floor
    @Query("SELECT COUNT(r) FROM Room r WHERE r.floorNumber = :floorNumber")
    int countByFloorNumber(@Param("floorNumber") Integer floorNumber);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.floorNumber = :floorNumber AND r.occupied = false")
    int countAvailableByFloorNumber(@Param("floorNumber") Integer floorNumber);

    // Statistical methods
    @Query("SELECT COUNT(r) FROM Room r")
    int countAllRooms();

    @Query("SELECT r FROM Room r WHERE r.roomNumber LIKE %:searchTerm% OR r.description LIKE %:searchTerm%")
    List<Room> searchRooms(@Param("searchTerm") String searchTerm);

}

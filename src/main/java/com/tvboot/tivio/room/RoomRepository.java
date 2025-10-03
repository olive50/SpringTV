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
    List<Room> findByStatus(Room.RoomStatus status);
    List<Room> findByRoomType(Room.RoomType roomType);
    List<Room> findByBuilding(String building);
    List<Room> findByFloorNumber(Integer floorNumber);

    // Complex queries
    @Query("SELECT r FROM Room r WHERE r.capacity >= :guests AND r.status = 'AVAILABLE'")
    List<Room> findAvailableRoomsForGuests(@Param("guests") int numberOfGuests);

    @Query("SELECT r FROM Room r WHERE r.pricePerNight BETWEEN :minPrice AND :maxPrice AND r.status = 'AVAILABLE'")
    List<Room> findAvailableRoomsByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                              @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT r FROM Room r WHERE r.roomNumber LIKE %:searchTerm% OR r.building LIKE %:searchTerm% OR r.description LIKE %:searchTerm%")
    List<Room> searchRooms(@Param("searchTerm") String searchTerm);

    // Terminal-related queries
    @Query("SELECT r FROM Room r JOIN r.terminals t WHERE t.id = :terminalCode")
    Optional<Room> findByTerminalId(@Param("terminalCode") Long terminalCode);

    @Query("SELECT r FROM Room r JOIN r.terminals t WHERE t.terminalCode = :terminalNumber")
    Optional<Room> findByTerminalNumber(@Param("terminalNumber") String terminalNumber);

    @Query("SELECT r FROM Room r WHERE r.id IN (SELECT t.room.id FROM Terminal t WHERE t.status = :status)")
    List<Room> findByTerminalStatus(@Param("status") String status);

    // ============ COUNT METHODS ============

    // Basic count methods
    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'AVAILABLE'")
    int countAvailableRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'OCCUPIED'")
    int countOccupiedRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'MAINTENANCE'")
    int countMaintenanceRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'CLEANING'")
    int countCleaningRooms();

    // Count by room type
    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType = :roomType")
    int countByRoomType(@Param("roomType") Room.RoomType roomType);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType = :roomType AND r.status = 'AVAILABLE'")
    int countAvailableByRoomType(@Param("roomType") Room.RoomType roomType);

    // Count by building
    @Query("SELECT COUNT(r) FROM Room r WHERE r.building = :building")
    int countByBuilding(@Param("building") String building);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.building = :building AND r.status = 'AVAILABLE'")
    int countAvailableByBuilding(@Param("building") String building);

    // Count by floor
    @Query("SELECT COUNT(r) FROM Room r WHERE r.floorNumber = :floorNumber")
    int countByFloorNumber(@Param("floorNumber") Integer floorNumber);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.floorNumber = :floorNumber AND r.status = 'AVAILABLE'")
    int countAvailableByFloorNumber(@Param("floorNumber") Integer floorNumber);

    // Count by price range
    @Query("SELECT COUNT(r) FROM Room r WHERE r.pricePerNight BETWEEN :minPrice AND :maxPrice")
    int countByPriceRange(@Param("minPrice") BigDecimal minPrice,
                          @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.pricePerNight BETWEEN :minPrice AND :maxPrice AND r.status = 'AVAILABLE'")
    long countAvailableByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.capacity >= :minOccupancy")
    int countByMinOccupancy(@Param("minOccupancy") Integer minOccupancy);

    // Count with terminals - PostgreSQL array_length function
    @Query("SELECT COUNT(r) FROM Room r WHERE SIZE(r.terminals) > 0")
    int countWithTerminals();

    @Query("SELECT COUNT(r) FROM Room r WHERE SIZE(r.terminals) = 0")
    int countWithoutTerminals();

    @Query("SELECT COUNT(r) FROM Room r WHERE SIZE(r.terminals) > :minTerminals")
    int countWithMinTerminals(@Param("minTerminals") int minTerminals);

    // Statistical methods
    @Query("SELECT COUNT(r) FROM Room r")
    int countAllRooms();

    @Query("SELECT AVG(r.pricePerNight) FROM Room r WHERE r.status = 'AVAILABLE'")
    Double findAveragePriceOfAvailableRooms();

    @Query("SELECT MAX(r.pricePerNight) FROM Room r WHERE r.status = 'AVAILABLE'")
    BigDecimal findMaxPriceOfAvailableRooms();

    @Query("SELECT MIN(r.pricePerNight) FROM Room r WHERE r.status = 'AVAILABLE'")
    BigDecimal findMinPriceOfAvailableRooms();

    @Query("SELECT AVG(r.capacity) FROM Room r")
    Double findAverageOccupancy();

    // Search count methods
    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomNumber LIKE %:searchTerm% OR r.building LIKE %:searchTerm% OR r.description LIKE %:searchTerm%")
    int countSearchResults(@Param("searchTerm") String searchTerm);

    // Count by multiple criteria
    @Query("SELECT COUNT(r) FROM Room r WHERE r.building = :building AND r.floorNumber = :floorNumber AND r.status = 'AVAILABLE'")
    int countAvailableByBuildingAndFloor(@Param("building") String building,
                                         @Param("floorNumber") Integer floorNumber);
}

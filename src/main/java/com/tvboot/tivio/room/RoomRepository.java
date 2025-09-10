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
    @Query("SELECT r FROM Room r JOIN r.terminals t WHERE t.id = :terminalId")
    Optional<Room> findByTerminalId(@Param("terminalId") Long terminalId);

//    @Query("SELECT r FROM Room r JOIN r.terminals t WHERE t.terminalNumber = :terminalNumber")
//    Optional<Room> findByTerminalNumber(@Param("terminalNumber") String terminalNumber);

    @Query("SELECT r FROM Room r WHERE r.id IN (SELECT t.room.id FROM Terminal t WHERE t.status = :status)")
    List<Room> findByTerminalStatus(@Param("status") String status);

    // ============ COUNT METHODS ============

    // Basic count methods
    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'AVAILABLE'")
    long countAvailableRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'OCCUPIED'")
    long countOccupiedRooms();

//    @Query("SELECT COUNT(r) FROM Room r WHERE r.status IN ('MAINTENANCE', 'OUT_OF_ORDER')")
//    long countMaintenanceRooms();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'CLEANING'")
    long countCleaningRooms();

    // Count by room type
    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType = :roomType")
    long countByRoomType(@Param("roomType") Room.RoomType roomType);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType = :roomType AND r.status = 'AVAILABLE'")
    long countAvailableByRoomType(@Param("roomType") Room.RoomType roomType);

    // Count by building
    @Query("SELECT COUNT(r) FROM Room r WHERE r.building = :building")
    long countByBuilding(@Param("building") String building);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.building = :building AND r.status = 'AVAILABLE'")
    long countAvailableByBuilding(@Param("building") String building);

    // Count by floor
    @Query("SELECT COUNT(r) FROM Room r WHERE r.floorNumber = :floorNumber")
    long countByFloorNumber(@Param("floorNumber") Integer floorNumber);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.floorNumber = :floorNumber AND r.status = 'AVAILABLE'")
    long countAvailableByFloorNumber(@Param("floorNumber") Integer floorNumber);

    // Count by price range
    @Query("SELECT COUNT(r) FROM Room r WHERE r.pricePerNight BETWEEN :minPrice AND :maxPrice")
    long countByPriceRange(@Param("minPrice") BigDecimal minPrice,
                           @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.pricePerNight BETWEEN :minPrice AND :maxPrice AND r.status = 'AVAILABLE'")
    long countAvailableByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice);

//    // Count by occupancy
//    @Query("SELECT COUNT(r) FROM Room r WHERE r.capacity >= :minOccupancy")
//    long countByMinOccupancy(@Param("minOccupancy") Integer minOccupancy);
//
//    @Query("SELECT COUNT(r) FROM Room r WHERE r.capacity <= :capacity")
//    long countByMaxOccupancy(@Param("capacity") Integer capacity);
//
//    @Query("SELECT COUNT(r) FROM Room r WHERE r.capacity BETWEEN :minOccupancy AND :capacity")
//    long countByOccupancyRange(@Param("minOccupancy") Integer minOccupancy,
//                               @Param("capacity") Integer capacity);

    // Count with channel package
    @Query("SELECT COUNT(r) FROM Room r WHERE r.channelPackage IS NOT NULL")
    long countWithChannelPackage();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.channelPackage IS NULL")
    long countWithoutChannelPackage();

    @Query("SELECT COUNT(r) FROM Room r WHERE r.channelPackage IS NOT NULL AND r.status = 'AVAILABLE'")
    long countAvailableWithChannelPackage();

    // Count with terminals
    @Query("SELECT COUNT(r) FROM Room r WHERE SIZE(r.terminals) > 0")
    long countWithTerminals();

    @Query("SELECT COUNT(r) FROM Room r WHERE SIZE(r.terminals) = 0")
    long countWithoutTerminals();

    @Query("SELECT COUNT(r) FROM Room r WHERE SIZE(r.terminals) > :minTerminals")
    long countWithMinTerminals(@Param("minTerminals") int minTerminals);

    // Statistical methods
    @Query("SELECT COUNT(r) FROM Room r")
    long countAllRooms();

    @Query("SELECT AVG(r.pricePerNight) FROM Room r WHERE r.status = 'AVAILABLE'")
    Double findAveragePriceOfAvailableRooms();

    @Query("SELECT MAX(r.pricePerNight) FROM Room r WHERE r.status = 'AVAILABLE'")
    BigDecimal findMaxPriceOfAvailableRooms();

    @Query("SELECT MIN(r.pricePerNight) FROM Room r WHERE r.status = 'AVAILABLE'")
    BigDecimal findMinPriceOfAvailableRooms();
//
//    @Query("SELECT AVG(r.capacity) FROM Room r")
//    Double findAverageOccupancy();

    // Search count methods
    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomNumber LIKE %:searchTerm% OR r.building LIKE %:searchTerm% OR r.description LIKE %:searchTerm%")
    long countSearchResults(@Param("searchTerm") String searchTerm);

    // Count by multiple criteria
    @Query("SELECT COUNT(r) FROM Room r WHERE r.building = :building AND r.floorNumber = :floorNumber AND r.status = 'AVAILABLE'")
    long countAvailableByBuildingAndFloor(@Param("building") String building,
                                          @Param("floorNumber") Integer floorNumber);

//    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType = :roomType AND r.capacity >= :minOccupancy AND r.status = 'AVAILABLE'")
//    long countAvailableByTypeAndMinOccupancy(@Param("roomType") Room.RoomType roomType,
//   @Query("SELECT COUNT(r) FROM Room r WHERE r.roomType = :roomType AND r.capacity >= :minOccupancy AND r.status = 'AVAILABLE'")
//    long countAvailableByTypeAndMinOccupancy(@Param("roomType") Room.RoomType roomType,
//                                             @Param("minOccupancy") Integer minOccupancy);
}
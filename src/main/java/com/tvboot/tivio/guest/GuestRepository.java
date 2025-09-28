package com.tvboot.tivio.guest;

import com.tvboot.tivio.common.enumeration.Gender;
import com.tvboot.tivio.common.enumeration.LoyaltyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    // ==================== BASIC FINDERS ====================
    Optional<Guest> findByPmsGuestId(String pmsGuestId);
    Optional<Guest> findByEmail(String email);
    List<Guest> findByFirstNameAndLastName(String firstName, String lastName);

    // ==================== SEARCH OPERATIONS ====================
    Page<Guest> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    @Query("SELECT g FROM Guest g WHERE " +
            "LOWER(g.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(g.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(g.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(g.pmsGuestId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(g.nationality) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Guest> findByAdvancedSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ==================== ATTRIBUTE FILTERING ====================
    List<Guest> findByNationality(String nationality);
    Page<Guest> findByNationality(String nationality, Pageable pageable);

    List<Guest> findByGender(Gender gender);
    Page<Guest> findByGender(Gender gender, Pageable pageable);

    List<Guest> findByVipStatusTrue();
    Page<Guest> findByVipStatus(Boolean vipStatus, Pageable pageable);

    List<Guest> findByLoyaltyLevel(LoyaltyLevel loyaltyLevel);
    Page<Guest> findByLoyaltyLevel(LoyaltyLevel loyaltyLevel, Pageable pageable);

    // ==================== DATE-BASED QUERIES ====================
    List<Guest> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT g FROM Guest g WHERE g.createdAt >= :startDate")
    List<Guest> findRecentGuests(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT g FROM Guest g WHERE g.createdAt >= :startDate")
    Page<Guest> findRecentGuests(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    // ==================== ROOM-RELATED QUERIES ====================
    @Query("SELECT g FROM Guest g WHERE g.room.id = :roomId")
    List<Guest> findByCurrentRoom(@Param("roomId") Long roomId);

    @Query("SELECT g FROM Guest g WHERE g.room.roomNumber = :roomNumber")
    List<Guest> findByRoomNumber(@Param("roomNumber") String roomNumber);

    @Query("SELECT g FROM Guest g WHERE g.room IS NULL")
    List<Guest> findGuestsWithoutRoom();

    @Query("SELECT g FROM Guest g WHERE g.room IS NULL")
    Page<Guest> findGuestsWithoutRoom(Pageable pageable);

    // ==================== CONTACT INFORMATION ====================
    @Query("SELECT g FROM Guest g WHERE g.phone = :phone OR g.email = :email")
    Optional<Guest> findByPhoneOrEmail(@Param("phone") String phone, @Param("email") String email);

    List<Guest> findByPhoneContaining(String phone);

    // ==================== COMPLEX FILTERING ====================
    @Query("SELECT g FROM Guest g WHERE " +
            "(:nationality IS NULL OR g.nationality = :nationality) AND " +
            "(:gender IS NULL OR g.gender = :gender) AND " +
            "(:vipStatus IS NULL OR g.vipStatus = :vipStatus) AND " +
            "(:loyaltyLevel IS NULL OR g.loyaltyLevel = :loyaltyLevel) AND " +
            "(:roomId IS NULL OR g.room.id = :roomId)")
    Page<Guest> findByMultipleCriteria(
            @Param("nationality") String nationality,
            @Param("gender") Gender gender,
            @Param("vipStatus") Boolean vipStatus,
            @Param("loyaltyLevel") LoyaltyLevel loyaltyLevel,
            @Param("roomId") Long roomId,
            Pageable pageable);

    // ==================== STATISTICAL QUERIES ====================
    @Query("SELECT COUNT(g) FROM Guest g WHERE g.vipStatus = true")
    long countVipGuests();

    @Query("SELECT COUNT(g) FROM Guest g WHERE g.createdAt >= :startDate")
    long countRecentGuests(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT g.nationality, COUNT(g) FROM Guest g GROUP BY g.nationality ORDER BY COUNT(g) DESC")
    List<Object[]> countGuestsByNationality();

    @Query("SELECT g.loyaltyLevel, COUNT(g) FROM Guest g GROUP BY g.loyaltyLevel")
    List<Object[]> countGuestsByLoyaltyLevel();

    @Query("SELECT g.gender, COUNT(g) FROM Guest g GROUP BY g.gender")
    List<Object[]> countGuestsByGender();

    // ==================== LANGUAGE QUERIES ====================
    @Query("SELECT g FROM Guest g WHERE g.language.id = :languageId")
    List<Guest> findByLanguageId(@Param("languageId") Long languageId);

    @Query("SELECT g FROM Guest g WHERE g.language.localeCode = :languageCode")
    List<Guest> findByLanguageCode(@Param("languageCode") String languageCode);

    // ==================== TOP GUESTS ====================
    @Query("SELECT g FROM Guest g WHERE g.vipStatus = true ORDER BY g.createdAt DESC")
    Page<Guest> findTopVipGuests(Pageable pageable);

    @Query("SELECT g FROM Guest g ORDER BY g.createdAt DESC")
    Page<Guest> findLatestGuests(Pageable pageable);

    // ==================== EXISTENCE CHECKS ====================
    boolean existsByPmsGuestId(String pmsGuestId);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // ==================== DASHBOARD STATISTICS ====================
    @Query("SELECT " +
            "COUNT(g), " +
            "COUNT(CASE WHEN g.vipStatus = true THEN 1 END), " +
            "COUNT(CASE WHEN g.createdAt >= :recentDate THEN 1 END) " +
            "FROM Guest g")
    Object[] getGuestStatistics(@Param("recentDate") LocalDateTime recentDate);

    // PostgreSQL-specific date extraction
    @Query("SELECT " +
            "EXTRACT(YEAR FROM g.createdAt) as year, " +
            "EXTRACT(MONTH FROM g.createdAt) as month, " +
            "COUNT(g) as count " +
            "FROM Guest g " +
            "WHERE g.createdAt >= :startDate " +
            "GROUP BY EXTRACT(YEAR FROM g.createdAt), EXTRACT(MONTH FROM g.createdAt) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyGuestStatistics(@Param("startDate") LocalDateTime startDate);
}
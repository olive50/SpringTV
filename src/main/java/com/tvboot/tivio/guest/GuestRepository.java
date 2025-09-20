package com.tvboot.tivio.guest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByGuestId(String guestId);
    Optional<Guest> findByEmail(String email);
    Optional<Guest> findByPassportNumber(String passportNumber);

    List<Guest> findByFirstNameAndLastName(String firstName, String lastName);
    List<Guest> findByNationality(String nationality);
    List<Guest> findByVipStatusTrue();
    List<Guest> findByLoyaltyLevel(Guest.LoyaltyLevel loyaltyLevel);

    Page<Guest> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    @Query("SELECT g FROM Guest g WHERE g.room.id = :roomId")
    List<Guest> findByCurrentRoom(@Param("roomId") Long roomId);

    @Query("SELECT g FROM Guest g WHERE g.phone = :phone OR g.email = :email")
    Optional<Guest> findByPhoneOrEmail(@Param("phone") String phone, @Param("email") String email);
}
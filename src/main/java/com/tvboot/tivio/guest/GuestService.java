package com.tvboot.tivio.guest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GuestService {

    private final GuestRepository guestRepository;

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public Page<Guest> getAllGuests(Pageable pageable) {
        return guestRepository.findAll(pageable);
    }

    public Optional<Guest> getGuestById(Long id) {
        return guestRepository.findById(id);
    }

    public Optional<Guest> getGuestByGuestId(String guestId) {
        return guestRepository.findByGuestId(guestId);
    }

    public Optional<Guest> getGuestByEmail(String email) {
        return guestRepository.findByEmail(email);
    }

    public Page<Guest> searchGuests(String searchTerm, Pageable pageable) {
        return guestRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
    }

    public List<Guest> getVipGuests() {
        return guestRepository.findByVipStatusTrue();
    }

    public List<Guest> getGuestsByLoyaltyLevel(Guest.LoyaltyLevel loyaltyLevel) {
        return guestRepository.findByLoyaltyLevel(loyaltyLevel);
    }

    public Guest createGuest(Guest guest) {
        // Generate unique guest ID if not provided
        if (guest.getGuestId() == null) {
            guest.setGuestId(generateGuestId());
        }

        // Validate unique constraints
        if (guestRepository.findByGuestId(guest.getGuestId()).isPresent()) {
            throw new RuntimeException("Guest ID already exists: " + guest.getGuestId());
        }

        if (guest.getEmail() != null && guestRepository.findByEmail(guest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + guest.getEmail());
        }

        if (guest.getPassportNumber() != null &&
                guestRepository.findByPassportNumber(guest.getPassportNumber()).isPresent()) {
            throw new RuntimeException("Passport number already exists: " + guest.getPassportNumber());
        }

        return guestRepository.save(guest);
    }

    public Guest updateGuest(Long id, Guest guestDetails) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guest not found: " + id));

        // Validate unique email if being updated
        if (guestDetails.getEmail() != null && !guestDetails.getEmail().equals(guest.getEmail())) {
            if (guestRepository.findByEmail(guestDetails.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists: " + guestDetails.getEmail());
            }
        }

        // Update fields
        guest.setFirstName(guestDetails.getFirstName());
        guest.setLastName(guestDetails.getLastName());
        guest.setEmail(guestDetails.getEmail());
        guest.setPhone(guestDetails.getPhone());
        guest.setNationality(guestDetails.getNationality());
        guest.setPassportNumber(guestDetails.getPassportNumber());
        guest.setIdCardNumber(guestDetails.getIdCardNumber());
        guest.setDateOfBirth(guestDetails.getDateOfBirth());
        guest.setGender(guestDetails.getGender());
        guest.setVipStatus(guestDetails.getVipStatus());
        guest.setLoyaltyLevel(guestDetails.getLoyaltyLevel());
        guest.setPreferredLanguage(guestDetails.getPreferredLanguage());
        guest.setSpecialRequests(guestDetails.getSpecialRequests());
        guest.setNotes(guestDetails.getNotes());

        return guestRepository.save(guest);
    }

    public void deleteGuest(Long id) {
        if (!guestRepository.existsById(id)) {
            throw new RuntimeException("Guest not found: " + id);
        }
        guestRepository.deleteById(id);
    }

    private String generateGuestId() {
        String prefix = "G";
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + uuid;
    }
}
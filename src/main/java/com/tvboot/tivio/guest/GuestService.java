package com.tvboot.tivio.guest;

import com.tvboot.tivio.common.enumeration.Gender;
import com.tvboot.tivio.common.enumeration.LoyaltyLevel;
import com.tvboot.tivio.guest.dto.GuestMapper;
import com.tvboot.tivio.guest.dto.GuestSearchDto;
import com.tvboot.tivio.guest.dto.GuestUpdateDto;

import com.tvboot.tivio.language.LanguageRepository;
import com.tvboot.tivio.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GuestService {

    private final GuestRepository guestRepository;
    private final LanguageRepository languageRepository;
    private final RoomRepository roomRepository;

    // Basic CRUD Operations
    @Cacheable(value = "guests", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<Guest> getAllGuests() {
        log.debug("Fetching all guests from database");
        return guestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Guest> getAllGuests(Pageable pageable) {
        log.debug("Fetching paged guests with pageable: {}", pageable);
        return guestRepository.findAll(pageable);
    }

    @Cacheable(value = "guest", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Guest> getGuestById(Long id) {
        log.debug("Fetching guest by ID: {}", id);
        return guestRepository.findById(id);
    }

    @Cacheable(value = "guest", key = "#guestId")
    @Transactional(readOnly = true)
    public Optional<Guest> getGuestByGuestId(String guestId) {
        log.debug("Fetching guest by PMS guest ID: {}", guestId);
        return guestRepository.findByPmsGuestId(guestId);
    }

    @Transactional(readOnly = true)
    public Optional<Guest> getGuestByEmail(String email) {
        log.debug("Fetching guest by email: {}", email);
        return guestRepository.findByEmail(email);
    }

    // Search Operations
    @Transactional(readOnly = true)
    public Page<Guest> searchGuests(String searchTerm, Pageable pageable) {
        log.debug("Searching guests with term: {}", searchTerm);
        return guestRepository.findByAdvancedSearch(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Guest> searchGuestsWithCriteria(GuestSearchDto searchDto, Pageable pageable) {
        log.debug("Searching guests with complex criteria: {}", searchDto);

        if (searchDto.getSearchTerm() != null && !searchDto.getSearchTerm().trim().isEmpty()) {
            return guestRepository.findByAdvancedSearch(searchDto.getSearchTerm(), pageable);
        }

        return guestRepository.findByMultipleCriteria(
                searchDto.getNationality(),
                searchDto.getGender(),
                searchDto.getVipStatus(),
                searchDto.getLoyaltyLevel(),
                searchDto.getRoomId(),
                pageable
        );
    }

    // VIP and Special Guests
    @Cacheable(value = "vipGuests")
    @Transactional(readOnly = true)
    public List<Guest> getVipGuests() {
        log.debug("Fetching VIP guests");
        return guestRepository.findByVipStatusTrue();
    }

    @Transactional(readOnly = true)
    public Page<Guest> getVipGuests(Pageable pageable) {
        log.debug("Fetching VIP guests with pagination");
        return guestRepository.findByVipStatus(true, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Guest> getRecentGuests(int days, Pageable pageable) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        log.debug("Fetching guests created since: {}", startDate);
        return guestRepository.findRecentGuests(startDate, pageable);
    }

    // Create, Update, Delete Operations
    @CacheEvict(value = {"guests", "vipGuests"}, allEntries = true)
    public Guest createGuest(Guest guest) {
        log.info("Creating new guest: {} {}", guest.getFirstName(), guest.getLastName());

        // Generate unique guest ID if not provided
        if (guest.getPmsGuestId() == null || guest.getPmsGuestId().trim().isEmpty()) {
            guest.setPmsGuestId(generateGuestId());
        }

        // Validate unique constraints
        validateGuestConstraints(guest, null);

        // Validate related entities
        validateRelatedEntities(guest);

        Guest savedGuest = guestRepository.save(guest);
        log.info("Guest created successfully with ID: {}", savedGuest.getId());
        return savedGuest;
    }

    @CacheEvict(value = {"guests", "vipGuests", "guest"}, allEntries = true)
    public Guest updateGuest(Long id, GuestUpdateDto updateDto, GuestMapper mapper) {
        log.info("Updating guest with ID: {}", id);

        Guest existingGuest = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guest not found with ID: " + id));

        // Use mapper to update the entity
        mapper.updateEntityFromDto(updateDto, existingGuest);

        // Validate unique constraints
        validateGuestConstraints(existingGuest, id);

        // Validate related entities
        validateRelatedEntities(existingGuest);

        Guest updatedGuest = guestRepository.save(existingGuest);
        log.info("Guest updated successfully with ID: {}", updatedGuest.getId());
        return updatedGuest;
    }

    @CacheEvict(value = {"guests", "vipGuests", "guest"}, allEntries = true)
    public void deleteGuest(Long id) {
        log.info("Deleting guest with ID: {}", id);

        if (!guestRepository.existsById(id)) {
            throw new RuntimeException("Guest not found with ID: " + id);
        }

        guestRepository.deleteById(id);
        log.info("Guest deleted successfully with ID: {}", id);
    }

    // Special Operations
    @CacheEvict(value = {"guests", "vipGuests", "guest"}, allEntries = true)
    public Guest updateVipStatus(Long id, Boolean vipStatus) {
        log.info("Updating VIP status for guest ID: {} to {}", id, vipStatus);

        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guest not found with ID: " + id));

        guest.setVipStatus(vipStatus);
        Guest updatedGuest = guestRepository.save(guest);
        log.info("VIP status updated successfully for guest ID: {}", id);
        return updatedGuest;
    }

    @CacheEvict(value = {"guests", "vipGuests", "guest"}, allEntries = true)
    public Guest assignGuestToRoom(Long guestId, Long roomId) {
        log.info("Assigning guest ID: {} to room ID: {}", guestId, roomId);

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found with ID: " + guestId));

        if (roomId != null) {
            if (!roomRepository.existsById(roomId)) {
                throw new RuntimeException("Room not found with ID: " + roomId);
            }
        }

        guest.setRoom(roomId != null ? roomRepository.getReferenceById(roomId) : null);
        Guest updatedGuest = guestRepository.save(guest);
        log.info("Guest assigned to room successfully");
        return updatedGuest;
    }

    // Filtering Operations
    @Transactional(readOnly = true)
    public Page<Guest> getGuestsByNationality(String nationality, Pageable pageable) {
        log.debug("Fetching guests by nationality: {}", nationality);
        return guestRepository.findByNationality(nationality, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Guest> getGuestsByGender(Gender gender, Pageable pageable) {
        log.debug("Fetching guests by gender: {}", gender);
        return guestRepository.findByGender(gender, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Guest> getGuestsByLoyaltyLevel(LoyaltyLevel loyaltyLevel, Pageable pageable) {
        log.debug("Fetching guests by loyalty level: {}", loyaltyLevel);
        return guestRepository.findByLoyaltyLevel(loyaltyLevel, pageable);
    }

    // Room-related Operations
    @Transactional(readOnly = true)
    public List<Guest> getGuestsByRoom(Long roomId) {
        log.debug("Fetching guests for room ID: {}", roomId);
        return guestRepository.findByCurrentRoom(roomId);
    }

    @Transactional(readOnly = true)
    public List<Guest> getGuestsByRoomNumber(String roomNumber) {
        log.debug("Fetching guests for room number: {}", roomNumber);
        return guestRepository.findByRoomNumber(roomNumber);
    }

    @Transactional(readOnly = true)
    public Page<Guest> getGuestsWithoutRoom(Pageable pageable) {
        log.debug("Fetching guests without assigned room");
        return guestRepository.findGuestsWithoutRoom(pageable);
    }

    // Statistics and Analytics
    @Transactional(readOnly = true)
    public Map<String, Object> getGuestStatistics() {
        log.debug("Calculating guest statistics");

        Map<String, Object> statistics = new HashMap<>();

        // Basic counts
        long totalGuests = guestRepository.count();
        long vipGuests = guestRepository.countVipGuests();
        long recentGuests = guestRepository.countRecentGuests(LocalDateTime.now().minusDays(30));

        statistics.put("totalGuests", totalGuests);
        statistics.put("vipGuests", vipGuests);
        statistics.put("recentGuests", recentGuests);
        statistics.put("vipPercentage", totalGuests > 0 ? (double) vipGuests / totalGuests * 100 : 0);

        // Distribution statistics
        Map<String, Long> nationalityStats = new HashMap<>();
        guestRepository.countGuestsByNationality().forEach(result ->
                nationalityStats.put((String) result[0], (Long) result[1]));
        statistics.put("nationalityDistribution", nationalityStats);

        Map<String, Long> loyaltyStats = new HashMap<>();
        guestRepository.countGuestsByLoyaltyLevel().forEach(result ->
                loyaltyStats.put(result[0].toString(), (Long) result[1]));
        statistics.put("loyaltyLevelDistribution", loyaltyStats);

        Map<String, Long> genderStats = new HashMap<>();
        guestRepository.countGuestsByGender().forEach(result ->
                genderStats.put(result[0].toString(), (Long) result[1]));
        statistics.put("genderDistribution", genderStats);

        log.debug("Guest statistics calculated: {}", statistics);
        return statistics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyStatistics(int monthsBack) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(monthsBack);
        List<Object[]> monthlyData = guestRepository.getMonthlyGuestStatistics(startDate);

        Map<String, Object> monthlyStats = new HashMap<>();
        monthlyData.forEach(result -> {
            String monthKey = result[0] + "-" + String.format("%02d", result[1]);
            monthlyStats.put(monthKey, result[2]);
        });

        return Map.of("monthlyRegistrations", monthlyStats);
    }

    // Language-related Operations
    @Transactional(readOnly = true)
    public List<Guest> getGuestsByLanguage(Long languageId) {
        log.debug("Fetching guests by language ID: {}", languageId);
        return guestRepository.findByLanguageId(languageId);
    }

    @Transactional(readOnly = true)
    public List<Guest> getGuestsByLanguageCode(String languageCode) {
        log.debug("Fetching guests by language code: {}", languageCode);
        return guestRepository.findByLanguageCode(languageCode);
    }

    // Utility Methods
    @Transactional(readOnly = true)
    public boolean existsByPmsGuestId(String pmsGuestId) {
        return guestRepository.existsByPmsGuestId(pmsGuestId);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return guestRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Guest> findByPhoneOrEmail(String phone, String email) {
        return guestRepository.findByPhoneOrEmail(phone, email);
    }

    // Top Guests
    @Transactional(readOnly = true)
    public Page<Guest> getTopVipGuests(Pageable pageable) {
        log.debug("Fetching top VIP guests");
        return guestRepository.findTopVipGuests(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Guest> getLatestGuests(Pageable pageable) {
        log.debug("Fetching latest registered guests");
        return guestRepository.findLatestGuests(pageable);
    }

    // Validation Methods
    private void validateGuestConstraints(Guest guest, Long excludeId) {
        // Validate unique PMS guest ID
        Optional<Guest> existingByPmsId = guestRepository.findByPmsGuestId(guest.getPmsGuestId());
        if (existingByPmsId.isPresent() &&
                (excludeId == null || !existingByPmsId.get().getId().equals(excludeId))) {
            throw new RuntimeException("Guest ID already exists: " + guest.getPmsGuestId());
        }

        // Validate unique email if provided
        if (guest.getEmail() != null && !guest.getEmail().trim().isEmpty()) {
            Optional<Guest> existingByEmail = guestRepository.findByEmail(guest.getEmail());
            if (existingByEmail.isPresent() &&
                    (excludeId == null || !existingByEmail.get().getId().equals(excludeId))) {
                throw new RuntimeException("Email already exists: " + guest.getEmail());
            }
        }
    }

    private void validateRelatedEntities(Guest guest) {
        // Validate language exists
        if (guest.getLanguage() != null && guest.getLanguage().getId() != null) {
            boolean languageExists = languageRepository.existsById(guest.getLanguage().getId());
            if (!languageExists) {
                throw new RuntimeException("Language not found with ID: " + guest.getLanguage().getId());
            }
        }

        // Validate room exists
        if (guest.getRoom() != null && guest.getRoom().getId() != null) {
            boolean roomExists = roomRepository.existsById(guest.getRoom().getId());
            if (!roomExists) {
                throw new RuntimeException("Room not found with ID: " + guest.getRoom().getId());
            }
        }
    }

    private String generateGuestId() {
        String prefix = "G";
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String guestId = prefix + uuid;

        // Ensure uniqueness
        while (guestRepository.findByPmsGuestId(guestId).isPresent()) {
            uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            guestId = prefix + uuid;
        }

        log.debug("Generated unique guest ID: {}", guestId);
        return guestId;
    }
}
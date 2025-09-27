// GuestMapper.java
package com.tvboot.tivio.guest.dto;

import com.tvboot.tivio.guest.Guest;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.room.Room;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface GuestMapper {

    // Entity to Response DTO
    @Mapping(target = "language", source = "language", qualifiedByName = "languageToDto")
    @Mapping(target = "room", source = "room", qualifiedByName = "roomToDto")
    GuestResponseDto toResponseDto(Guest guest);

    // Entity to Summary DTO
    @Mapping(target = "currentRoom", source = "room.roomNumber")
    GuestSummaryDto toSummaryDto(Guest guest);

    // Create DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "language", source = "languageId", qualifiedByName = "languageIdToLanguage")
    @Mapping(target = "room", source = "roomId", qualifiedByName = "roomIdToRoom")
    Guest toEntity(GuestCreateDto createDto);

    // Update DTO to Entity (for partial updates)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pmsGuestId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "language", source = "languageId", qualifiedByName = "languageIdToLanguage")
    @Mapping(target = "room", source = "roomId", qualifiedByName = "roomIdToRoom")
    void updateEntityFromDto(GuestUpdateDto updateDto, @MappingTarget Guest guest);

    // List mappings
    List<GuestResponseDto> toResponseDtoList(List<Guest> guests);
    List<GuestSummaryDto> toSummaryDtoList(List<Guest> guests);

    // Page mapping
    default Page<GuestResponseDto> toResponseDtoPage(Page<Guest> guestPage) {
        return guestPage.map(this::toResponseDto);
    }

    default Page<GuestSummaryDto> toSummaryDtoPage(Page<Guest> guestPage) {
        return guestPage.map(this::toSummaryDto);
    }

    // Custom mapping methods for nested objects
    @Named("languageToDto")
    default GuestResponseDto.LanguageDto languageToDto(Language language) {
        if (language == null) {
            return null;
        }
        return GuestResponseDto.LanguageDto.builder()
                .id(language.getId())
                .name(language.getName())
                .code(language.getLocaleCode())
                .build();
    }

    @Named("roomToDto")
    default GuestResponseDto.RoomDto roomToDto(Room room) {
        if (room == null) {
            return null;
        }
        return GuestResponseDto.RoomDto.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .floorNumber(room.getFloorNumber())
                .build();
    }

    @Named("languageIdToLanguage")
    default Language languageIdToLanguage(Long languageId) {
        if (languageId == null) {
            return null;
        }
        Language language = new Language();
        language.setId(languageId);
        return language;
    }

    @Named("roomIdToRoom")
    default Room roomIdToRoom(Long roomId) {
        if (roomId == null) {
            return null;
        }
        Room room = new Room();
        room.setId(roomId);
        return room;
    }

    // Validation mapping
    default GuestCreateDto entityToCreateDto(Guest guest) {
        if (guest == null) {
            return null;
        }

        return GuestCreateDto.builder()
                .pmsGuestId(guest.getPmsGuestId())
                .firstName(guest.getFirstName())
                .lastName(guest.getLastName())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .nationality(guest.getNationality())
                .dateOfBirth(guest.getDateOfBirth())
                .gender(guest.getGender())
                .vipStatus(guest.getVipStatus())
                .loyaltyLevel(guest.getLoyaltyLevel())
                .languageId(guest.getLanguage() != null ? guest.getLanguage().getId() : null)
                .roomId(guest.getRoom() != null ? guest.getRoom().getId() : null)
                .build();
    }

    default GuestUpdateDto entityToUpdateDto(Guest guest) {
        if (guest == null) {
            return null;
        }

        return GuestUpdateDto.builder()
                .firstName(guest.getFirstName())
                .lastName(guest.getLastName())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .nationality(guest.getNationality())
                .dateOfBirth(guest.getDateOfBirth())
                .gender(guest.getGender())
                .vipStatus(guest.getVipStatus())
                .loyaltyLevel(guest.getLoyaltyLevel())
                .languageId(guest.getLanguage() != null ? guest.getLanguage().getId() : null)
                .roomId(guest.getRoom() != null ? guest.getRoom().getId() : null)
                .build();
    }
}
package com.tvboot.tivio.terminal.dto;

import com.tvboot.tivio.room.Room;
import com.tvboot.tivio.room.RoomMapper;
import com.tvboot.tivio.terminal.Terminal;
import org.mapstruct.*;

import java.util.List;

// Updated TerminalMapper with Room mapping
@Mapper(componentModel = "spring",
        uses = {RoomMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TerminalMapper {

    //@Mapping(target = "room", source = "room", qualifiedByName = "roomToSummaryDto")
    TerminalDto toDto(Terminal terminal);

    List<TerminalDto> toDtoList(List<Terminal> terminals);

    @Mapping(target = "terminalId", source = "terminalId")
    @Mapping(target = "isOnline", source = "isOnline")
    @Mapping(target = "responseTime", source = "responseTime")
    @Mapping(target = "uptime", source = "uptime")
    @Mapping(target = "lastPingTime", source = "lastPingTime")
    @Mapping(target = "lastSeen", source = "lastSeen")
    TerminalConnectivityDto toConnectivityDto(Terminal terminal);

    List<TerminalConnectivityDto> toConnectivityDtoList(List<Terminal> terminals);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "INACTIVE")
    @Mapping(target = "lastSeen", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "responseTime", ignore = true)
    @Mapping(target = "uptime", ignore = true)
    @Mapping(target = "lastPingTime", ignore = true)
    @Mapping(target = "isOnline", constant = "false")
    @Mapping(target = "room", source = "roomId", qualifiedByName = "roomIdToRoom")
    Terminal fromCreateRequest(TerminalCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastSeen", ignore = true)
    @Mapping(target = "room", source = "roomId", qualifiedByName = "roomIdToRoom")
    void updateFromRequest(@MappingTarget Terminal terminal, TerminalUpdateRequest request);

    // Helper methods
    @Named("roomIdToRoom")
    default Room roomIdToRoom(Long roomId) {
        if (roomId == null) {
            return null;
        }
        return Room.builder().id(roomId).build();
    }

    @Named("roomToSummaryDto")
    default RoomSummaryDto roomToSummaryDto(Room room) {
        if (room == null) {
            return null;
        }
        return RoomSummaryDto.builder()
                .id(room.getId())
                .roomName(room.getRoomNumber())
                .roomCapacity(room.getCapacity())
                .build();
    }
}
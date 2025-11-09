package com.tvboot.tivio.terminal.dto;

import com.tvboot.tivio.room.Room;
import com.tvboot.tivio.room.RoomMapper;
import com.tvboot.tivio.terminal.Terminal;
import com.tvboot.tivio.tvchannel.TvChannel;
import com.tvboot.tivio.tvchannel.dto.TvChannelUpdateDTO;
import org.mapstruct.*;

import java.util.List;

// Updated TerminalMapper with Room mapping
@Mapper(componentModel = "spring",
        uses = {RoomMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TerminalMapper {

    //@Mapping(target = "room", source = "room", qualifiedByName = "roomToSummaryDto")
//    @Mapping(target = "roomNumber", source = "room.roomNumber")
    TerminalDto toDto(Terminal terminal);

    List<TerminalDto> toDtoList(List<Terminal> terminals);

    @Mapping(target = "terminalCode", source = "terminalCode")
    @Mapping(target = "isOnline", source = "isOnline")

    @Mapping(target = "uptime", source = "uptime")
    @Mapping(target = "lastSeen", source = "lastSeen")
    TerminalConnectivityDto toConnectivityDto(Terminal terminal);

    List<TerminalConnectivityDto> toConnectivityDtoList(List<Terminal> terminals);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastSeen", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "uptime", ignore = true)
    @Mapping(target = "isOnline", constant = "false")
    Terminal toEntity(TerminalCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastSeen", ignore = true)
    void updateFromRequest(@MappingTarget Terminal terminal, TerminalUpdateRequest request);

}
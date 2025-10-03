package com.tvboot.tivio.room;

import com.tvboot.tivio.room.dto.RoomRequest;
import com.tvboot.tivio.room.dto.RoomResponse;
import com.tvboot.tivio.room.dto.RoomSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoomMapper {

    Room toEntity(RoomRequest request);

    RoomResponse toResponse(Room room);

    RoomSummary toSummary(Room room);

    void updateEntityFromRequest(RoomRequest request, @MappingTarget Room room);

    @Mapping(target = "available", expression = "java(room.isAvailable())")
    @Mapping(target = "fullRoomIdentifier", expression = "java(room.getFullRoomIdentifier())")
    RoomResponse roomToRoomResponse(Room room);

    @Mapping(target = "available", expression = "java(room.isAvailable())")
    @Mapping(target = "fullRoomIdentifier", expression = "java(room.getFullRoomIdentifier())")
    RoomSummary roomToRoomSummary(Room room);
}
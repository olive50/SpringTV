package com.tvboot.tivio.tv.dto;

import com.tvboot.tivio.tv.TvChannel;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TvChannelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    TvChannel toEntity(TvChannelCreateDTO dto);

    @Mapping(target = "logoUrl", expression = "java(generateLogoUrl(entity))")
    TvChannelDTO toDTO(TvChannel entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    void updateEntityFromDTO(TvChannelUpdateDTO dto, @MappingTarget TvChannel entity);



    default String generateLogoUrl(TvChannel entity) {
        if (entity.getLogoPath() != null && entity.getId() != null) {
            return "/api/tv-channels/" + entity.getId() + "/logo";
        }
        return null;
    }
}
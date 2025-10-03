package com.tvboot.tivio.smartapp.amenity;

import com.tvboot.tivio.common.util.FileUrlBuilder;
import com.tvboot.tivio.smartapp.amenity.dto.*;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = FileUrlBuilder.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AmenityMapper {

    @Autowired
    protected FileUrlBuilder fileUrlBuilder;

    // --- LIST DTO ---
    @Mapping(target = "thumbnailUrl", source = "thumbnailPath", qualifiedByName = "imageUrlBuilder")
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryToString")
    @Mapping(target = "categoryDisplayName", source = "category", qualifiedByName = "categoryToDisplayName")
    public abstract AmenityListDTO toListDTO(Amenity amenity);

    public abstract List<AmenityListDTO> toListDTOs(List<Amenity> amenities);

    // --- DETAIL DTO ---
    @Mapping(target = "imageUrl", source = "imagePath", qualifiedByName = "imageUrlBuilder")
    @Mapping(target = "thumbnailUrl", source = "thumbnailPath", qualifiedByName = "imageUrlBuilder")
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryToString")
    @Mapping(target = "categoryDisplayName", source = "category", qualifiedByName = "categoryToDisplayName")
    public abstract AmenityDetailDTO toDetailDTO(Amenity amenity);

    // --- CREATE DTO -> ENTITY ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "stringToCategory")
    public abstract Amenity toEntity(AmenityCreateDTO dto);

    // --- UPDATE DTO -> ENTITY ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "stringToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntityFromDTO(AmenityUpdateDTO dto, @MappingTarget Amenity amenity);

    // --- ENUM HELPERS ---
    @Named("categoryToString")
    protected String categoryToString(Amenity.AmenityCategory category) {
        return category != null ? category.name() : null;
    }

    @Named("categoryToDisplayName")
    protected String categoryToDisplayName(Amenity.AmenityCategory category) {
        return category != null ? category.getDisplayName() : null;
    }

    @Named("stringToCategory")
    protected Amenity.AmenityCategory stringToCategory(String category) {
        try {
            return category != null ? Amenity.AmenityCategory.valueOf(category) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

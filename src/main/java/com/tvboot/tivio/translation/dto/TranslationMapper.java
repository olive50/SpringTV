package com.tvboot.tivio.translation.dto;

import com.tvboot.tivio.translation.Translation;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TranslationMapper {

    /**
     * Map Translation entity to TranslationDtoWithCode
     * Maps language.iso6391 to iso6391 field
     */
    @Mapping(source = "language.iso6391", target = "iso6391")
    TranslationDtoWithCode toDto(Translation translation);

    /**
     * Map TranslationDtoWithCode to Translation entity
     * Note: Language object must be set separately in service layer
     */
    @Mapping(target = "language", ignore = true)
    @Mapping(source = "iso6391", target = "language.iso6391")
    Translation toEntity(TranslationDtoWithCode dto);

    /**
     * Map list of Translation entities to list of DTOs
     */
    List<TranslationDtoWithCode> toDtoList(List<Translation> translations);

    /**
     * Update existing Translation entity from DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(TranslationDtoWithCode dto, @MappingTarget Translation translation);
}
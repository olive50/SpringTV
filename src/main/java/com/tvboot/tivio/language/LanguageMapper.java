// LanguageMapper.java - Fixed version
package com.tvboot.tivio.language;

import com.tvboot.tivio.language.dto.*;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LanguageMapper {

    // Remove @Value annotations - they cause issues with MapStruct
    private static final String BASE_URL = "http://localhost:8080";
    private static final String FLAGS_PATH = "/uploads/flags";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Language toEntity(LanguageCreateDTO dto);

    @Mapping(target = "flagUrl", source = "flagPath", qualifiedByName = "generateFlagUrl")
    public abstract LanguageResponseDTO toDTO(Language entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "iso6391", ignore = true) // ISO code should not be updated
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntityFromDTO(LanguageUpdateDTO dto, @MappingTarget Language entity);

    @Named("generateFlagUrl")
    protected String generateFlagUrl(String flagPath) {
        if (flagPath != null && !flagPath.isEmpty()) {
            return BASE_URL + FLAGS_PATH + "/" + flagPath;
        }
        return null;
    }

    @AfterMapping
    protected void setDefaults(@MappingTarget Language entity, LanguageCreateDTO dto) {
        if (entity.getCharset() == null) {
            entity.setCharset(Language.DEFAULT_CHARSET);
        }
        if (entity.getDateFormat() == null) {
            entity.setDateFormat(Language.DEFAULT_DATE_FORMAT);
        }
        if (entity.getTimeFormat() == null) {
            entity.setTimeFormat(Language.DEFAULT_TIME_FORMAT);
        }
        if (entity.getIsRtl() == null) {
            entity.setIsRtl(false);
        }
        if (entity.getIsAdminEnabled() == null) {
            entity.setIsAdminEnabled(true);
        }
        if (entity.getIsGuestEnabled() == null) {
            entity.setIsGuestEnabled(false);
        }
        if (entity.getDisplayOrder() == null) {
            entity.setDisplayOrder(0);
        }
        if (entity.getDecimalSeparator() == null) {
            entity.setDecimalSeparator('.');
        }
        if (entity.getThousandsSeparator() == null) {
            entity.setThousandsSeparator(',');
        }
    }
}
package com.filmreview.mapper;

import com.filmreview.dto.LanguageDto;
import com.filmreview.entity.Language;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for converting Language entities to LanguageDtos.
 */
@Mapper(componentModel = "spring")
public interface LanguageDtoMapper {

  /**
   * Map Language entity to LanguageDto.
   */
  LanguageDto toDto(Language language);

  /**
   * Map List of Language entities to List of LanguageDtos.
   */
  List<LanguageDto> toDtoList(List<Language> languages);
}

package com.filmreview.mapper;

import com.filmreview.dto.TitleDto;
import com.filmreview.entity.Title;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper interface for Title to TitleDto mapping.
 * MapStruct will generate the implementation.
 * Genres are handled separately in TitleDtoMapper wrapper component.
 */
@Mapper(componentModel = "spring")
public interface TitleDtoMapperHelper {

  @Mapping(target = "type", expression = "java(title.getType() != null ? title.getType().name() : null)")
  @Mapping(target = "genres", ignore = true) // Handle genres in TitleDtoMapper wrapper
  TitleDto toDto(Title title);
}

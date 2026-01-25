package com.filmreview.mapper;

import com.filmreview.dto.GenreDto;
import com.filmreview.entity.Genre;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for converting Genre entities to GenreDtos.
 */
@Mapper(componentModel = "spring")
public interface GenreDtoMapper {

  /**
   * Map Genre entity to GenreDto.
   */
  GenreDto toDto(Genre genre);

  /**
   * Map List of Genre entities to List of GenreDtos.
   */
  List<GenreDto> toDtoList(List<Genre> genres);
}

package com.filmreview.mapper;

import com.filmreview.dto.TitleDto;
import com.filmreview.entity.Title;
import com.filmreview.repository.TitleGenreRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for converting Title entities to TitleDtos.
 * Uses GenreDtoMapper for genre mapping.
 * 
 * This is a Spring component wrapper around the MapStruct-generated mapper
 * to handle genre fetching from the repository.
 */
@Component
public class TitleDtoMapper {

  private final TitleDtoMapperHelper mapper;
  private final TitleGenreRepository titleGenreRepository;
  private final GenreDtoMapper genreDtoMapper;

  @Autowired
  public TitleDtoMapper(
      TitleDtoMapperHelper mapper,
      TitleGenreRepository titleGenreRepository,
      GenreDtoMapper genreDtoMapper) {
    this.mapper = mapper;
    this.titleGenreRepository = titleGenreRepository;
    this.genreDtoMapper = genreDtoMapper;
  }

  /**
   * Map Title entity to TitleDto.
   */
  public TitleDto toDto(Title title) {
    if (title == null) {
      return null;
    }

    // Use MapStruct mapper for basic field mapping
    TitleDto dto = mapper.toDto(title);

    // Fetch and map genres using GenreDtoMapper
    if (title.getId() != null) {
      List<com.filmreview.entity.Genre> genres = titleGenreRepository.findGenresByTitleId(title.getId());
      dto.setGenres(genreDtoMapper.toDtoList(genres));
    }

    return dto;
  }

  /**
   * Map Page of Title entities to Page of TitleDtos.
   */
  public Page<TitleDto> toDtoPage(Page<Title> titlePage) {
    if (titlePage == null) {
      return null;
    }

    List<TitleDto> dtoList = titlePage.getContent().stream()
        .map(this::toDto)
        .collect(Collectors.toList());

    return new PageImpl<>(
        dtoList,
        titlePage.getPageable(),
        titlePage.getTotalElements());
  }

}

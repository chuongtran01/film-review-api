package com.filmreview.controller;

import com.filmreview.dto.TitleDto;
import com.filmreview.entity.Title;
import com.filmreview.mapper.TitleDtoMapper;
import com.filmreview.service.TitleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TitleControllerTest {

  @Mock
  private TitleService titleService;

  @Mock
  private TitleDtoMapper titleDtoMapper;

  @InjectMocks
  private TitleController titleController;

  private Title testMovie;
  private Title testTVShow;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    // Create test movie
    testMovie = new Title();
    testMovie.setId(UUID.randomUUID());
    testMovie.setType(Title.TitleType.movie);
    testMovie.setTmdbId(603);
    testMovie.setTitle("The Matrix");
    testMovie.setSlug("the-matrix-1999");

    // Create test TV show
    testTVShow = new Title();
    testTVShow.setId(UUID.randomUUID());
    testTVShow.setType(Title.TitleType.tv_show);
    testTVShow.setTmdbId(1396);
    testTVShow.setTitle("Breaking Bad");
    testTVShow.setSlug("breaking-bad");

    pageable = PageRequest.of(0, 20);
  }

  @Test
  void testGetTitles_PopularMovies() {
    // Arrange
    Page<Title> moviePage = new PageImpl<>(List.of(testMovie), pageable, 1);
    TitleDto movieDTO = createTitleDto(testMovie);
    Page<TitleDto> dtoPage = new PageImpl<>(List.of(movieDTO), pageable, 1);

    when(titleService.getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class)))
        .thenReturn(moviePage);
    when(titleDtoMapper.toDtoPage(moviePage)).thenReturn(dtoPage);

    // Act
    ResponseEntity<Page<TitleDto>> response = titleController.getTitles(
        null, "movie", null, null, null, 1, 20, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getTotalElements());
    assertEquals(1, response.getBody().getContent().size());
    assertEquals("The Matrix", response.getBody().getContent().get(0).getTitle());
    verify(titleService).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleService, never()).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
    verify(titleDtoMapper).toDtoPage(moviePage);
  }

  @Test
  void testGetTitles_PopularTVShows() {
    // Arrange
    Page<Title> tvPage = new PageImpl<>(List.of(testTVShow), pageable, 1);
    TitleDto tvDTO = createTitleDto(testTVShow);
    Page<TitleDto> dtoPage = new PageImpl<>(List.of(tvDTO), pageable, 1);

    when(titleService.getPopularTVShows(anyString(), anyInt(), any(Pageable.class)))
        .thenReturn(tvPage);
    when(titleDtoMapper.toDtoPage(tvPage)).thenReturn(dtoPage);

    // Act
    ResponseEntity<Page<TitleDto>> response = titleController.getTitles(
        null, "tv_show", null, null, null, 1, 20, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getTotalElements());
    assertEquals(1, response.getBody().getContent().size());
    assertEquals("Breaking Bad", response.getBody().getContent().get(0).getTitle());
    verify(titleService).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
    verify(titleService, never()).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleDtoMapper).toDtoPage(tvPage);
  }

  @Test
  void testGetTitles_PopularWithoutType_ReturnsEmpty() {
    // Act
    ResponseEntity<Page<TitleDto>> response = titleController.getTitles(
        null, null, null, null, null, 1, 20, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getTotalElements());
    assertTrue(response.getBody().getContent().isEmpty());
    verify(titleService, never()).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleService, never()).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
    verify(titleDtoMapper, never()).toDtoPage(any());
  }

  @Test
  void testGetTitles_PopularWithInvalidType_ReturnsEmpty() {
    // Act
    ResponseEntity<Page<TitleDto>> response = titleController.getTitles(
        null, "invalid_type", null, null, null, 1, 20, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getTotalElements());
    verify(titleService, never()).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleService, never()).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
    verify(titleDtoMapper, never()).toDtoPage(any());
  }

  @Test
  void testGetTitles_NonPopularSort_ReturnsEmpty() {
    // Act
    ResponseEntity<Page<TitleDto>> response = titleController.getTitles(
        null, "movie", null, null, null, 1, 20, "top_rated");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getTotalElements());
    verify(titleService, never()).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleService, never()).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
    verify(titleDtoMapper, never()).toDtoPage(any());
  }

  @Test
  void testGetTitles_Pagination() {
    // Arrange
    Page<Title> moviePage = new PageImpl<>(List.of(testMovie), PageRequest.of(1, 10), 25);
    TitleDto movieDTO = createTitleDto(testMovie);
    Page<TitleDto> dtoPage = new PageImpl<>(List.of(movieDTO), PageRequest.of(1, 10), 25);

    when(titleService.getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class)))
        .thenReturn(moviePage);
    when(titleDtoMapper.toDtoPage(moviePage)).thenReturn(dtoPage);

    // Act
    ResponseEntity<Page<TitleDto>> response = titleController.getTitles(
        null, "movie", null, null, null, 2, 10, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(25, response.getBody().getTotalElements());
    assertEquals(1, response.getBody().getContent().size());
    verify(titleService).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleDtoMapper).toDtoPage(moviePage);
  }

  @Test
  void testGetTitleByIdentifier_BySlug() {
    // Arrange
    TitleDto movieDTO = createTitleDto(testMovie);
    when(titleService.getTitleBySlug("the-matrix-1999"))
        .thenReturn(testMovie);
    when(titleDtoMapper.toDto(testMovie)).thenReturn(movieDTO);

    // Act
    ResponseEntity<TitleDto> response = titleController.getTitleByIdentifier("the-matrix-1999", null);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("The Matrix", response.getBody().getTitle());
    verify(titleService).getTitleBySlug("the-matrix-1999");
    verify(titleService, never()).getTitleByTmdbId(anyInt());
    verify(titleDtoMapper).toDto(testMovie);
  }

  @Test
  void testGetTitleByIdentifier_ByTmdbId_WithType_Movie() {
    // Arrange
    TitleDto movieDTO = createTitleDto(testMovie);
    when(titleService.getTitleByTmdbId(603, "movie"))
        .thenReturn(testMovie);
    when(titleDtoMapper.toDto(testMovie)).thenReturn(movieDTO);

    // Act
    ResponseEntity<TitleDto> response = titleController.getTitleByIdentifier("603", "movie");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("The Matrix", response.getBody().getTitle());
    verify(titleService).getTitleByTmdbId(603, "movie");
    verify(titleService, never()).getTitleBySlug(anyString());
    verify(titleDtoMapper).toDto(testMovie);
  }

  @Test
  void testGetTitleByIdentifier_ByTmdbId_WithType_TvShow() {
    // Arrange
    TitleDto tvDTO = createTitleDto(testTVShow);
    when(titleService.getTitleByTmdbId(1396, "tv_show"))
        .thenReturn(testTVShow);
    when(titleDtoMapper.toDto(testTVShow)).thenReturn(tvDTO);

    // Act
    ResponseEntity<TitleDto> response = titleController.getTitleByIdentifier("1396", "tv_show");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Breaking Bad", response.getBody().getTitle());
    verify(titleService).getTitleByTmdbId(1396, "tv_show");
    verify(titleService, never()).getTitleBySlug(anyString());
    verify(titleDtoMapper).toDto(testTVShow);
  }

  @Test
  void testGetTitleByIdentifier_ByTmdbId_WithInvalidType_ReturnsBadRequest() {
    // Act
    ResponseEntity<TitleDto> response = titleController.getTitleByIdentifier("603", "invalid_type");

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(titleService, never()).getTitleByTmdbId(anyInt(), anyString());
    verify(titleService, never()).getTitleBySlug(anyString());
    verify(titleDtoMapper, never()).toDto(any());
  }

  @Test
  void testGetTitleByIdentifier_ByTmdbId_WithoutType_ReturnsBadRequest() {
    // Act
    ResponseEntity<TitleDto> response = titleController.getTitleByIdentifier("603", null);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(titleService, never()).getTitleByTmdbId(anyInt());
    verify(titleService, never()).getTitleBySlug(anyString());
    verify(titleDtoMapper, never()).toDto(any());
  }

  @Test
  void testGetTitleByIdentifier_BySlug_NotFound() {
    // Arrange
    when(titleService.getTitleBySlug("non-existent-slug"))
        .thenThrow(new com.filmreview.exception.NotFoundException("Title not found"));

    // Act
    ResponseEntity<TitleDto> response = titleController.getTitleByIdentifier("non-existent-slug", null);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(titleService).getTitleBySlug("non-existent-slug");
    verify(titleDtoMapper, never()).toDto(any());
  }

  /**
   * Helper method to create a TitleDto from a Title entity for testing.
   */
  private TitleDto createTitleDto(Title title) {
    TitleDto dto = new TitleDto();
    dto.setId(title.getId());
    dto.setTmdbId(title.getTmdbId());
    dto.setImdbId(title.getImdbId());
    dto.setType(title.getType() != null ? title.getType().name() : null);
    dto.setTitle(title.getTitle());
    dto.setOriginalTitle(title.getOriginalTitle());
    dto.setSlug(title.getSlug());
    dto.setSynopsis(title.getSynopsis());
    dto.setReleaseDate(title.getReleaseDate());
    dto.setRuntime(title.getRuntime());
    dto.setPosterUrl(title.getPosterUrl());
    dto.setBackdropUrl(title.getBackdropUrl());
    dto.setStatus(title.getStatus());
    dto.setUserRatingAvg(title.getUserRatingAvg());
    dto.setUserRatingCount(title.getUserRatingCount());
    dto.setNumberOfSeasons(title.getNumberOfSeasons());
    dto.setNumberOfEpisodes(title.getNumberOfEpisodes());
    dto.setFirstAirDate(title.getFirstAirDate());
    dto.setCreatedAt(title.getCreatedAt());
    dto.setUpdatedAt(title.getUpdatedAt());
    dto.setGenres(Collections.emptyList()); // Empty list for tests
    return dto;
  }
}

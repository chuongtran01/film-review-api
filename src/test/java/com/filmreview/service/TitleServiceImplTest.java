package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.dto.tmdb.TmdbPageResponse;
import com.filmreview.dto.tmdb.TmdbTvSeriesResponse;
import com.filmreview.entity.Genre;
import com.filmreview.entity.Title;
import com.filmreview.entity.TitleGenre;
import com.filmreview.entity.TitleGenreId;
import com.filmreview.exception.NotFoundException;
import com.filmreview.mapper.TitleMapper;
import com.filmreview.repository.GenreRepository;
import com.filmreview.repository.TitleGenreRepository;
import com.filmreview.repository.TitleRepository;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TitleServiceImplTest {

  @Mock
  private TitleRepository titleRepository;

  @Mock
  private GenreRepository genreRepository;

  @Mock
  private TitleGenreRepository titleGenreRepository;

  @Mock
  private TmdbService tmdbService;

  @Mock
  private TitleMapper titleMapper;

  @InjectMocks
  private TitleServiceImpl titleService;

  private Title testMovie;
  private Title testTVShow;
  private UUID testMovieId;
  private UUID testTVShowId;

  @BeforeEach
  void setUp() {
    testMovieId = UUID.randomUUID();
    testTVShowId = UUID.randomUUID();

    testMovie = new Title();
    testMovie.setId(testMovieId);
    testMovie.setType(Title.TitleType.movie);
    testMovie.setTmdbId(603);
    testMovie.setTitle("The Matrix");

    testTVShow = new Title();
    testTVShow.setId(testTVShowId);
    testTVShow.setType(Title.TitleType.tv_show);
    testTVShow.setTmdbId(1396);
    testTVShow.setTitle("Breaking Bad");
  }

  @Test
  void testFetchAndSaveTvSeries_Success() {
    // Arrange
    Integer tmdbId = 1396;
    TmdbTvSeriesResponse tvSeriesResponse = createTvSeriesResponse(tmdbId);
    Title savedTitle = new Title();
    savedTitle.setId(testTVShowId);
    savedTitle.setTmdbId(tmdbId);
    savedTitle.setTitle("Breaking Bad");

    when(tmdbService.getTvSeriesDetails(tmdbId)).thenReturn(tvSeriesResponse);
    when(titleMapper.toTitle(tvSeriesResponse)).thenReturn(testTVShow);
    when(titleRepository.save(testTVShow)).thenReturn(savedTitle);
    when(genreRepository.findById(18)).thenReturn(Optional.empty());
    when(genreRepository.findById(80)).thenReturn(Optional.empty());
    when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(titleGenreRepository.existsById(any(TitleGenreId.class))).thenReturn(false);
    when(titleGenreRepository.save(any(TitleGenre.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Title result = titleService.fetchAndSaveTvSeries(tmdbId);

    // Assert
    assertNotNull(result);
    assertEquals(tmdbId, result.getTmdbId());
    verify(tmdbService).getTvSeriesDetails(tmdbId);
    verify(titleMapper).toTitle(tvSeriesResponse);
    verify(titleRepository).save(testTVShow);
    verify(genreRepository, times(2)).findById(anyInt());
    verify(titleGenreRepository, times(2)).save(any(TitleGenre.class));
  }

  @Test
  void testFetchAndSaveTvSeries_TmdbReturnsNull_ThrowsNotFoundException() {
    // Arrange
    Integer tmdbId = 999999;
    when(tmdbService.getTvSeriesDetails(tmdbId)).thenReturn(null);

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> titleService.fetchAndSaveTvSeries(tmdbId));
    assertEquals("TV series not found in TMDB: " + tmdbId, exception.getMessage());
    verify(tmdbService).getTvSeriesDetails(tmdbId);
    verify(titleMapper, never()).toTitle(any(TmdbMovieResponse.class));
    verify(titleMapper, never()).toTitle(any(TmdbTvSeriesResponse.class));
    verify(titleRepository, never()).save(any());
  }

  @Test
  void testFetchAndSaveTvSeries_WithExistingGenres() {
    // Arrange
    Integer tmdbId = 1396;
    TmdbTvSeriesResponse tvSeriesResponse = createTvSeriesResponse(tmdbId);
    Title savedTitle = new Title();
    savedTitle.setId(testTVShowId);
    savedTitle.setTmdbId(tmdbId);

    Genre existingGenre = new Genre();
    existingGenre.setId(18);
    existingGenre.setName("Drama");

    when(tmdbService.getTvSeriesDetails(tmdbId)).thenReturn(tvSeriesResponse);
    when(titleMapper.toTitle(tvSeriesResponse)).thenReturn(testTVShow);
    when(titleRepository.save(testTVShow)).thenReturn(savedTitle);
    when(genreRepository.findById(18)).thenReturn(Optional.of(existingGenre));
    when(genreRepository.findById(80)).thenReturn(Optional.empty());
    when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(titleGenreRepository.existsById(any(TitleGenreId.class))).thenReturn(false);
    when(titleGenreRepository.save(any(TitleGenre.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Title result = titleService.fetchAndSaveTvSeries(tmdbId);

    // Assert
    assertNotNull(result);
    verify(genreRepository).findById(18);
    verify(genreRepository, never()).save(existingGenre); // Existing genre should not be saved again
    verify(genreRepository).findById(80);
    verify(genreRepository).save(argThat(genre -> genre.getId().equals(80))); // New genre should be saved
  }

  @Test
  void testFetchAndSaveTvSeries_WithExistingTitleGenreRelationship() {
    // Arrange
    Integer tmdbId = 1396;
    TmdbTvSeriesResponse tvSeriesResponse = createTvSeriesResponse(tmdbId);
    Title savedTitle = new Title();
    savedTitle.setId(testTVShowId);
    savedTitle.setTmdbId(tmdbId);

    Genre genre = new Genre();
    genre.setId(18);
    genre.setName("Drama");

    when(tmdbService.getTvSeriesDetails(tmdbId)).thenReturn(tvSeriesResponse);
    when(titleMapper.toTitle(tvSeriesResponse)).thenReturn(testTVShow);
    when(titleRepository.save(testTVShow)).thenReturn(savedTitle);
    when(genreRepository.findById(18)).thenReturn(Optional.of(genre));
    when(genreRepository.findById(80)).thenReturn(Optional.empty());
    when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(titleGenreRepository.existsById(any(TitleGenreId.class))).thenReturn(true); // Relationship already exists

    // Act
    Title result = titleService.fetchAndSaveTvSeries(tmdbId);

    // Assert
    assertNotNull(result);
    verify(titleGenreRepository, never()).save(any(TitleGenre.class)); // Should not save duplicate relationship
  }

  @Test
  void testGetTitleByTmdbId_WithType_Movie() {
    // Arrange
    Integer tmdbId = 603;
    TmdbMovieResponse movieResponse = createMovieResponse(tmdbId);
    when(titleRepository.findByTmdbId(tmdbId)).thenReturn(Optional.empty());
    when(tmdbService.getMovieDetails(tmdbId)).thenReturn(movieResponse);
    when(titleMapper.toTitle(movieResponse)).thenReturn(testMovie);
    when(titleRepository.save(testMovie)).thenReturn(testMovie);
    when(genreRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(titleGenreRepository.existsById(any(TitleGenreId.class))).thenReturn(false);
    when(titleGenreRepository.save(any(TitleGenre.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Title result = titleService.getTitleByTmdbId(tmdbId, "movie");

    // Assert
    assertNotNull(result);
    verify(titleRepository).findByTmdbId(tmdbId);
    verify(tmdbService).getMovieDetails(tmdbId);
    verify(tmdbService, never()).getTvSeriesDetails(anyInt());
  }

  @Test
  void testGetTitleByTmdbId_WithType_TvShow() {
    // Arrange
    Integer tmdbId = 1396;
    when(titleRepository.findByTmdbId(tmdbId)).thenReturn(Optional.empty());
    TmdbTvSeriesResponse tvSeriesResponse = createTvSeriesResponse(tmdbId);
    when(tmdbService.getTvSeriesDetails(tmdbId)).thenReturn(tvSeriesResponse);
    when(titleMapper.toTitle(tvSeriesResponse)).thenReturn(testTVShow);
    when(titleRepository.save(testTVShow)).thenReturn(testTVShow);
    when(genreRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(titleGenreRepository.existsById(any(TitleGenreId.class))).thenReturn(false);
    when(titleGenreRepository.save(any(TitleGenre.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Title result = titleService.getTitleByTmdbId(tmdbId, "tv_show");

    // Assert
    assertNotNull(result);
    verify(titleRepository).findByTmdbId(tmdbId);
    verify(tmdbService).getTvSeriesDetails(tmdbId);
    verify(tmdbService, never()).getMovieDetails(anyInt());
  }

  @Test
  void testGetTitleByTmdbId_WithType_InvalidType_ThrowsIllegalArgumentException() {
    // Arrange
    Integer tmdbId = 603;
    when(titleRepository.findByTmdbId(tmdbId)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> titleService.getTitleByTmdbId(tmdbId, "invalid_type"));
    assertEquals("Invalid type: invalid_type. Must be 'movie' or 'tv_show'", exception.getMessage());
    verify(titleRepository).findByTmdbId(tmdbId);
    verify(tmdbService, never()).getMovieDetails(anyInt());
    verify(tmdbService, never()).getTvSeriesDetails(anyInt());
  }

  @Test
  void testGetTitleByTmdbId_WithType_AlreadyInDatabase() {
    // Arrange
    Integer tmdbId = 1396;
    when(titleRepository.findByTmdbId(tmdbId)).thenReturn(Optional.of(testTVShow));

    // Act
    Title result = titleService.getTitleByTmdbId(tmdbId, "tv_show");

    // Assert
    assertNotNull(result);
    assertEquals(testTVShow, result);
    verify(titleRepository).findByTmdbId(tmdbId);
    verify(tmdbService, never()).getTvSeriesDetails(anyInt());
    verify(titleMapper, never()).toTitle(any(TmdbMovieResponse.class));
    verify(titleMapper, never()).toTitle(any(TmdbTvSeriesResponse.class));
    verify(titleRepository, never()).save(any());
  }

  @Test
  void testFetchAndSaveTvSeries_WithEmptyGenres() {
    // Arrange
    Integer tmdbId = 1396;
    TmdbTvSeriesResponse tvSeriesResponse = createTvSeriesResponse(tmdbId);
    tvSeriesResponse.setGenres(null); // No genres

    Title savedTitle = new Title();
    savedTitle.setId(testTVShowId);
    savedTitle.setTmdbId(tmdbId);

    when(tmdbService.getTvSeriesDetails(tmdbId)).thenReturn(tvSeriesResponse);
    when(titleMapper.toTitle(tvSeriesResponse)).thenReturn(testTVShow);
    when(titleRepository.save(testTVShow)).thenReturn(savedTitle);

    // Act
    Title result = titleService.fetchAndSaveTvSeries(tmdbId);

    // Assert
    assertNotNull(result);
    verify(genreRepository, never()).findById(anyInt());
    verify(titleGenreRepository, never()).save(any(TitleGenre.class));
  }

  // ========== getTitleById Tests ==========

  @Test
  void testGetTitleById_Success() {
    // Arrange
    when(titleRepository.findById(testMovieId)).thenReturn(Optional.of(testMovie));

    // Act
    Title result = titleService.getTitleById(testMovieId);

    // Assert
    assertNotNull(result);
    assertEquals(testMovieId, result.getId());
    assertEquals("The Matrix", result.getTitle());
    verify(titleRepository).findById(testMovieId);
  }

  @Test
  void testGetTitleById_NotFound() {
    // Arrange
    UUID nonExistentId = UUID.randomUUID();
    when(titleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> titleService.getTitleById(nonExistentId));
    assertEquals("Title not found with id: " + nonExistentId, exception.getMessage());
    verify(titleRepository).findById(nonExistentId);
  }

  // ========== getTitleBySlug Tests ==========

  @Test
  void testGetTitleBySlug_Success() {
    // Arrange
    String slug = "the-matrix-1999";
    testMovie.setSlug(slug);
    when(titleRepository.findBySlug(slug)).thenReturn(Optional.of(testMovie));

    // Act
    Title result = titleService.getTitleBySlug(slug);

    // Assert
    assertNotNull(result);
    assertEquals(slug, result.getSlug());
    assertEquals("The Matrix", result.getTitle());
    verify(titleRepository).findBySlug(slug);
  }

  @Test
  void testGetTitleBySlug_NotFound() {
    // Arrange
    String nonExistentSlug = "non-existent-slug";
    when(titleRepository.findBySlug(nonExistentSlug)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> titleService.getTitleBySlug(nonExistentSlug));
    assertEquals("Title not found with slug: " + nonExistentSlug, exception.getMessage());
    verify(titleRepository).findBySlug(nonExistentSlug);
  }

  // ========== fetchAndSaveMovie Tests ==========

  @Test
  void testFetchAndSaveMovie_Success() {
    // Arrange
    Integer tmdbId = 603;
    TmdbMovieResponse movieResponse = createMovieResponse(tmdbId);
    Title savedTitle = new Title();
    savedTitle.setId(testMovieId);
    savedTitle.setTmdbId(tmdbId);
    savedTitle.setTitle("The Matrix");

    when(tmdbService.getMovieDetails(tmdbId)).thenReturn(movieResponse);
    when(titleMapper.toTitle(movieResponse)).thenReturn(testMovie);
    when(titleRepository.save(testMovie)).thenReturn(savedTitle);
    when(genreRepository.findById(28)).thenReturn(Optional.empty());
    when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(titleGenreRepository.existsById(any(TitleGenreId.class))).thenReturn(false);
    when(titleGenreRepository.save(any(TitleGenre.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Title result = titleService.fetchAndSaveMovie(tmdbId);

    // Assert
    assertNotNull(result);
    assertEquals(tmdbId, result.getTmdbId());
    verify(tmdbService).getMovieDetails(tmdbId);
    verify(titleMapper).toTitle(movieResponse);
    verify(titleRepository).save(testMovie);
    verify(genreRepository).findById(28);
    verify(titleGenreRepository).save(any(TitleGenre.class));
  }

  @Test
  void testFetchAndSaveMovie_TmdbReturnsNull_ThrowsNotFoundException() {
    // Arrange
    Integer tmdbId = 999999;
    when(tmdbService.getMovieDetails(tmdbId)).thenReturn(null);

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> titleService.fetchAndSaveMovie(tmdbId));
    assertEquals("Movie not found in TMDB: " + tmdbId, exception.getMessage());
    verify(tmdbService).getMovieDetails(tmdbId);
    verify(titleMapper, never()).toTitle(any(TmdbMovieResponse.class));
    verify(titleRepository, never()).save(any());
  }

  @Test
  void testFetchAndSaveMovie_WithExistingGenres() {
    // Arrange
    Integer tmdbId = 603;
    TmdbMovieResponse movieResponse = createMovieResponse(tmdbId);
    Title savedTitle = new Title();
    savedTitle.setId(testMovieId);
    savedTitle.setTmdbId(tmdbId);

    Genre existingGenre = new Genre();
    existingGenre.setId(28);
    existingGenre.setName("Action");

    when(tmdbService.getMovieDetails(tmdbId)).thenReturn(movieResponse);
    when(titleMapper.toTitle(movieResponse)).thenReturn(testMovie);
    when(titleRepository.save(testMovie)).thenReturn(savedTitle);
    when(genreRepository.findById(28)).thenReturn(Optional.of(existingGenre));
    when(titleGenreRepository.existsById(any(TitleGenreId.class))).thenReturn(false);
    when(titleGenreRepository.save(any(TitleGenre.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Title result = titleService.fetchAndSaveMovie(tmdbId);

    // Assert
    assertNotNull(result);
    verify(genreRepository).findById(28);
    verify(genreRepository, never()).save(existingGenre); // Existing genre should not be saved again
  }

  @Test
  void testFetchAndSaveMovie_WithEmptyGenres() {
    // Arrange
    Integer tmdbId = 603;
    TmdbMovieResponse movieResponse = createMovieResponse(tmdbId);
    movieResponse.setGenres(null); // No genres

    Title savedTitle = new Title();
    savedTitle.setId(testMovieId);
    savedTitle.setTmdbId(tmdbId);

    when(tmdbService.getMovieDetails(tmdbId)).thenReturn(movieResponse);
    when(titleMapper.toTitle(movieResponse)).thenReturn(testMovie);
    when(titleRepository.save(testMovie)).thenReturn(savedTitle);

    // Act
    Title result = titleService.fetchAndSaveMovie(tmdbId);

    // Assert
    assertNotNull(result);
    verify(genreRepository, never()).findById(anyInt());
    verify(titleGenreRepository, never()).save(any(TitleGenre.class));
  }

  // ========== searchTitles Tests ==========

  @Test
  void testSearchTitles_Success() {
    // Arrange
    String query = "matrix";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Title> expectedPage = new PageImpl<>(Arrays.asList(testMovie), pageable, 1);

    when(titleRepository.searchTitles(query.trim(), pageable)).thenReturn(expectedPage);

    // Act
    Page<Title> result = titleService.searchTitles(query, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    verify(titleRepository).searchTitles(query.trim(), pageable);
    verify(titleRepository, never()).searchTitlesByType(anyString(), any(), any(Pageable.class));
  }

  @Test
  void testSearchTitles_WithTypeFilter_Movie() {
    // Arrange
    String query = "matrix";
    String type = "movie";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Title> expectedPage = new PageImpl<>(Arrays.asList(testMovie), pageable, 1);

    when(titleRepository.searchTitlesByType(query.trim(), Title.TitleType.movie, pageable))
        .thenReturn(expectedPage);

    // Act
    Page<Title> result = titleService.searchTitles(query, type, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(titleRepository).searchTitlesByType(query.trim(), Title.TitleType.movie, pageable);
    verify(titleRepository, never()).searchTitles(anyString(), any(Pageable.class));
  }

  @Test
  void testSearchTitles_WithTypeFilter_TvShow() {
    // Arrange
    String query = "breaking";
    String type = "tv_show";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Title> expectedPage = new PageImpl<>(Arrays.asList(testTVShow), pageable, 1);

    when(titleRepository.searchTitlesByType(query.trim(), Title.TitleType.tv_show, pageable))
        .thenReturn(expectedPage);

    // Act
    Page<Title> result = titleService.searchTitles(query, type, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(titleRepository).searchTitlesByType(query.trim(), Title.TitleType.tv_show, pageable);
  }

  @Test
  void testSearchTitles_EmptyQuery() {
    // Arrange
    String query = "";
    Pageable pageable = PageRequest.of(0, 20);

    // Act
    Page<Title> result = titleService.searchTitles(query, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());
    verify(titleRepository, never()).searchTitles(anyString(), any(Pageable.class));
    verify(titleRepository, never()).searchTitlesByType(anyString(), any(), any(Pageable.class));
  }

  @Test
  void testSearchTitles_NullQuery() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);

    // Act
    Page<Title> result = titleService.searchTitles(null, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());
    verify(titleRepository, never()).searchTitles(anyString(), any(Pageable.class));
  }

  @Test
  void testSearchTitles_WhitespaceQuery() {
    // Arrange
    String query = "   ";
    Pageable pageable = PageRequest.of(0, 20);

    // Act
    Page<Title> result = titleService.searchTitles(query, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    verify(titleRepository, never()).searchTitles(anyString(), any(Pageable.class));
  }

  @Test
  void testSearchTitles_WithInvalidType() {
    // Arrange
    String query = "matrix";
    String type = "invalid_type";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Title> expectedPage = new PageImpl<>(Arrays.asList(testMovie), pageable, 1);

    // Invalid type should be ignored, search without type filter
    when(titleRepository.searchTitles(query.trim(), pageable)).thenReturn(expectedPage);

    // Act
    Page<Title> result = titleService.searchTitles(query, type, pageable);

    // Assert
    assertNotNull(result);
    verify(titleRepository).searchTitles(query.trim(), pageable);
    verify(titleRepository, never()).searchTitlesByType(anyString(), any(), any(Pageable.class));
  }

  // ========== getPopularMovies Tests ==========

  @Test
  void testGetPopularMovies_Success() {
    // Arrange
    String language = "en-US";
    int page = 1;
    String region = "US";
    Pageable pageable = PageRequest.of(0, 20);

    TmdbPageResponse.TmdbMovieItem movieItem = createTmdbMovieItem(603, "The Matrix");
    Page<TmdbPageResponse.TmdbMovieItem> tmdbPage = new PageImpl<>(
        Arrays.asList(movieItem), PageRequest.of(0, 20), 100);

    when(tmdbService.getPopularMovies(language, 1, region)).thenReturn(tmdbPage);
    when(titleRepository.findByTmdbId(603)).thenReturn(Optional.empty());
    when(tmdbService.getImageUrl(anyString(), anyString())).thenAnswer(invocation -> {
      String path = invocation.getArgument(0);
      return "https://image.tmdb.org" + path;
    });

    // Act
    Page<Title> result = titleService.getPopularMovies(language, page, region, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(100, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    verify(tmdbService).getPopularMovies(language, 1, region);
    verify(titleRepository).findByTmdbId(603);
    verify(tmdbService, atLeastOnce()).getImageUrl(anyString(), anyString());
  }

  @Test
  void testGetPopularMovies_WithExistingTitleInDB() {
    // Arrange
    String language = "en-US";
    int page = 1;
    String region = "US";
    Pageable pageable = PageRequest.of(0, 20);

    TmdbPageResponse.TmdbMovieItem movieItem = createTmdbMovieItem(603, "The Matrix");
    Page<TmdbPageResponse.TmdbMovieItem> tmdbPage = new PageImpl<>(
        Arrays.asList(movieItem), PageRequest.of(0, 20), 100);

    when(tmdbService.getPopularMovies(language, 1, region)).thenReturn(tmdbPage);
    when(titleRepository.findByTmdbId(603)).thenReturn(Optional.of(testMovie));

    // Act
    Page<Title> result = titleService.getPopularMovies(language, page, region, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(testMovie, result.getContent().get(0)); // Should return existing title from DB
    verify(tmdbService).getPopularMovies(language, 1, region);
    verify(titleRepository).findByTmdbId(603);
  }

  @Test
  void testGetPopularMovies_Pagination() {
    // Arrange
    String language = "en-US";
    int page = 2;
    String region = "US";
    Pageable pageable = PageRequest.of(1, 6); // Page 2, size 6

    // Create 20 items for TMDB page 1
    List<TmdbPageResponse.TmdbMovieItem> page1Items = Arrays.asList(
        createTmdbMovieItem(1, "Movie 1"),
        createTmdbMovieItem(2, "Movie 2"),
        createTmdbMovieItem(3, "Movie 3"),
        createTmdbMovieItem(4, "Movie 4"),
        createTmdbMovieItem(5, "Movie 5"),
        createTmdbMovieItem(6, "Movie 6"),
        createTmdbMovieItem(7, "Movie 7"),
        createTmdbMovieItem(8, "Movie 8"),
        createTmdbMovieItem(9, "Movie 9"),
        createTmdbMovieItem(10, "Movie 10"),
        createTmdbMovieItem(11, "Movie 11"),
        createTmdbMovieItem(12, "Movie 12"),
        createTmdbMovieItem(13, "Movie 13"),
        createTmdbMovieItem(14, "Movie 14"),
        createTmdbMovieItem(15, "Movie 15"),
        createTmdbMovieItem(16, "Movie 16"),
        createTmdbMovieItem(17, "Movie 17"),
        createTmdbMovieItem(18, "Movie 18"),
        createTmdbMovieItem(19, "Movie 19"),
        createTmdbMovieItem(20, "Movie 20")
    );

    Page<TmdbPageResponse.TmdbMovieItem> tmdbPage1 = new PageImpl<>(
        page1Items, PageRequest.of(0, 20), 100);

    when(tmdbService.getPopularMovies(language, 1, region)).thenReturn(tmdbPage1);
    when(titleRepository.findByTmdbId(anyInt())).thenReturn(Optional.empty());
    when(tmdbService.getImageUrl(anyString(), anyString())).thenAnswer(invocation -> {
      String path = invocation.getArgument(0);
      return "https://image.tmdb.org" + path;
    });

    // Act
    Page<Title> result = titleService.getPopularMovies(language, page, region, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(6, result.getContent().size()); // Should return 6 items (page 2, size 6)
    verify(tmdbService).getPopularMovies(language, 1, region);
  }

  // ========== getPopularTVShows Tests ==========

  @Test
  void testGetPopularTVShows_Success() {
    // Arrange
    String language = "en-US";
    int page = 1;
    Pageable pageable = PageRequest.of(0, 20);

    TmdbPageResponse.TmdbTvSeriesItem tvItem = createTmdbTvSeriesItem(1396, "Breaking Bad");
    Page<TmdbPageResponse.TmdbTvSeriesItem> tmdbPage = new PageImpl<>(
        Arrays.asList(tvItem), PageRequest.of(0, 20), 100);

    when(tmdbService.getPopularTVShows(language, 1)).thenReturn(tmdbPage);
    when(titleRepository.findByTmdbId(1396)).thenReturn(Optional.empty());
    when(tmdbService.getImageUrl(anyString(), anyString())).thenAnswer(invocation -> {
      String path = invocation.getArgument(0);
      return "https://image.tmdb.org" + path;
    });

    // Act
    Page<Title> result = titleService.getPopularTVShows(language, page, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(100, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    verify(tmdbService).getPopularTVShows(language, 1);
    verify(titleRepository).findByTmdbId(1396);
    verify(tmdbService, atLeastOnce()).getImageUrl(anyString(), anyString());
  }

  @Test
  void testGetPopularTVShows_WithExistingTitleInDB() {
    // Arrange
    String language = "en-US";
    int page = 1;
    Pageable pageable = PageRequest.of(0, 20);

    TmdbPageResponse.TmdbTvSeriesItem tvItem = createTmdbTvSeriesItem(1396, "Breaking Bad");
    Page<TmdbPageResponse.TmdbTvSeriesItem> tmdbPage = new PageImpl<>(
        Arrays.asList(tvItem), PageRequest.of(0, 20), 100);

    when(tmdbService.getPopularTVShows(language, 1)).thenReturn(tmdbPage);
    when(titleRepository.findByTmdbId(1396)).thenReturn(Optional.of(testTVShow));

    // Act
    Page<Title> result = titleService.getPopularTVShows(language, page, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(testTVShow, result.getContent().get(0)); // Should return existing title from DB
    verify(tmdbService).getPopularTVShows(language, 1);
    verify(titleRepository).findByTmdbId(1396);
  }

  // Helper methods
  private TmdbPageResponse.TmdbMovieItem createTmdbMovieItem(Integer id, String title) {
    TmdbPageResponse.TmdbMovieItem item = new TmdbPageResponse.TmdbMovieItem();
    item.setId(id);
    item.setTitle(title);
    item.setOriginalTitle(title);
    item.setOverview("Test overview");
    item.setReleaseDate("1999-03-31");
    item.setPosterPath("/poster.jpg");
    item.setBackdropPath("/backdrop.jpg");
    item.setVoteAverage(8.5);
    item.setVoteCount(1000);
    return item;
  }

  private TmdbPageResponse.TmdbTvSeriesItem createTmdbTvSeriesItem(Integer id, String name) {
    TmdbPageResponse.TmdbTvSeriesItem item = new TmdbPageResponse.TmdbTvSeriesItem();
    item.setId(id);
    item.setName(name);
    item.setOriginalName(name);
    item.setOverview("Test overview");
    item.setFirstAirDate("2008-01-20");
    item.setPosterPath("/poster.jpg");
    item.setBackdropPath("/backdrop.jpg");
    item.setVoteAverage(9.5);
    item.setVoteCount(2000);
    return item;
  }

  private TmdbMovieResponse createMovieResponse(Integer tmdbId) {
    TmdbMovieResponse response = new TmdbMovieResponse();
    response.setId(tmdbId);
    response.setTitle("The Matrix");
    response.setOverview("A computer hacker learns about the true nature of reality");
    response.setReleaseDate(LocalDate.of(1999, 3, 31));

    TmdbMovieResponse.TmdbGenre genre = new TmdbMovieResponse.TmdbGenre();
    genre.setId(28);
    genre.setName("Action");
    response.setGenres(Arrays.asList(genre));

    return response;
  }

  private TmdbTvSeriesResponse createTvSeriesResponse(Integer tmdbId) {
    TmdbTvSeriesResponse response = new TmdbTvSeriesResponse();
    response.setId(tmdbId);
    response.setName("Breaking Bad");
    response.setOverview("A high school chemistry teacher turned methamphetamine manufacturer");
    response.setFirstAirDate(LocalDate.of(2008, 1, 20));
    response.setNumberOfSeasons(5);
    response.setNumberOfEpisodes(62);
    response.setStatus("Ended");

    TmdbTvSeriesResponse.TmdbGenre genre1 = new TmdbTvSeriesResponse.TmdbGenre();
    genre1.setId(18);
    genre1.setName("Drama");

    TmdbTvSeriesResponse.TmdbGenre genre2 = new TmdbTvSeriesResponse.TmdbGenre();
    genre2.setId(80);
    genre2.setName("Crime");

    response.setGenres(Arrays.asList(genre1, genre2));

    return response;
  }
}

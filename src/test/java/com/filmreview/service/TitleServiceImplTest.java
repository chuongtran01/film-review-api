package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbMovieResponse;
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

import java.time.LocalDate;
import java.util.Arrays;
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

  // Helper methods
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

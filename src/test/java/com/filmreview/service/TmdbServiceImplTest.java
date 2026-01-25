package com.filmreview.service;

import com.filmreview.config.TmdbConfig;
import com.filmreview.dto.tmdb.TmdbGenreInfo;
import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.dto.tmdb.TmdbPageResponse;
import com.filmreview.dto.tmdb.TmdbTvSeriesResponse;
import com.filmreview.mapper.TmdbMovieMapper;
import com.filmreview.mapper.TmdbTvSeriesMapper;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbGenre;
import info.movito.themoviedbapi.TmdbMovieLists;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.TmdbTvSeriesLists;
import info.movito.themoviedbapi.model.core.Genre;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.TvSeriesResultsPage;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TmdbServiceImplTest {

  @Mock
  private TmdbConfig tmdbConfig;

  @Mock
  private TmdbMovieMapper tmdbMovieMapper;

  @Mock
  private TmdbTvSeriesMapper tmdbTvSeriesMapper;

  @Mock
  private TmdbApi tmdbApi;

  @Mock
  private TmdbMovies tmdbMovies;

  @Mock
  private TmdbMovieLists tmdbMovieLists;

  @Mock
  private TmdbTvSeries tmdbTvSeries;

  @Mock
  private TmdbTvSeriesLists tmdbTvSeriesLists;

  @Mock
  private TmdbGenre tmdbGenre;

  @InjectMocks
  private TmdbServiceImpl tmdbService;

  private static final String TEST_API_KEY = "test-api-key";
  private static final String TEST_IMAGE_BASE_URL = "https://image.tmdb.org/t/p";
  private static final String DEFAULT_LANGUAGE = "en-US";

  @BeforeEach
  void setUp() throws Exception {
    // Setup config mocks (used by all tests)
    lenient().when(tmdbConfig.getApiKey()).thenReturn(TEST_API_KEY);
    lenient().when(tmdbConfig.getImageBaseUrl()).thenReturn(TEST_IMAGE_BASE_URL);

    // Create service instance
    tmdbService = new TmdbServiceImpl(tmdbConfig, tmdbMovieMapper, tmdbTvSeriesMapper);

    // Use reflection to inject mocked dependencies after construction
    setPrivateField(tmdbService, "tmdbApi", tmdbApi);
    setPrivateField(tmdbService, "tmdbMovies", tmdbMovies);
    setPrivateField(tmdbService, "tmdbMoviesLists", tmdbMovieLists);
    setPrivateField(tmdbService, "tmdbTvSeries", tmdbTvSeries);
    setPrivateField(tmdbService, "tmdbTvSeriesLists", tmdbTvSeriesLists);
    setPrivateField(tmdbService, "tmdbGenre", tmdbGenre);
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void testGetMovieDetails_Success() throws Exception {
    // Arrange
    Integer tmdbId = 603;
    MovieDb movieDb = mock(MovieDb.class);
    TmdbMovieResponse expectedResponse = new TmdbMovieResponse();
    expectedResponse.setId(tmdbId);
    expectedResponse.setTitle("The Matrix");

    when(tmdbMovies.getDetails(tmdbId, DEFAULT_LANGUAGE, MovieAppendToResponse.CREDITS))
        .thenReturn(movieDb);
    when(tmdbMovieMapper.toMovieResponse(movieDb)).thenReturn(expectedResponse);

    // Act
    TmdbMovieResponse result = tmdbService.getMovieDetails(tmdbId);

    // Assert
    assertNotNull(result);
    assertEquals(tmdbId, result.getId());
    assertEquals("The Matrix", result.getTitle());
    verify(tmdbMovies).getDetails(tmdbId, DEFAULT_LANGUAGE, MovieAppendToResponse.CREDITS);
    verify(tmdbMovieMapper).toMovieResponse(movieDb);
  }

  @Test
  void testGetMovieDetails_TmdbException_ReturnsNull() throws Exception {
    // Arrange
    Integer tmdbId = 999999;
    when(tmdbMovies.getDetails(tmdbId, DEFAULT_LANGUAGE, MovieAppendToResponse.CREDITS))
        .thenThrow(new TmdbException("Movie not found"));

    // Act
    TmdbMovieResponse result = tmdbService.getMovieDetails(tmdbId);

    // Assert
    assertNull(result);
    verify(tmdbMovies).getDetails(tmdbId, DEFAULT_LANGUAGE, MovieAppendToResponse.CREDITS);
    verify(tmdbMovieMapper, never()).toMovieResponse(any());
  }

  @Test
  void testGetMovieDetails_GenericException_ThrowsRuntimeException() throws Exception {
    // Arrange
    Integer tmdbId = 603;
    when(tmdbMovies.getDetails(tmdbId, DEFAULT_LANGUAGE, MovieAppendToResponse.CREDITS))
        .thenThrow(new RuntimeException("Network error"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> tmdbService.getMovieDetails(tmdbId));
    assertEquals("Failed to fetch movie details from TMDB", exception.getMessage());
    verify(tmdbMovies).getDetails(tmdbId, DEFAULT_LANGUAGE, MovieAppendToResponse.CREDITS);
  }

  @Test
  void testGetTvSeriesDetails_Success() throws Exception {
    // Arrange
    Integer tmdbId = 1399;
    TvSeriesDb tvSeriesDb = mock(TvSeriesDb.class);
    TmdbTvSeriesResponse expectedResponse = new TmdbTvSeriesResponse();
    expectedResponse.setId(tmdbId);
    expectedResponse.setName("Game of Thrones");

    when(tmdbTvSeries.getDetails(tmdbId, DEFAULT_LANGUAGE)).thenReturn(tvSeriesDb);
    when(tmdbTvSeriesMapper.toTvSeriesResponse(tvSeriesDb)).thenReturn(expectedResponse);

    // Act
    TmdbTvSeriesResponse result = tmdbService.getTvSeriesDetails(tmdbId);

    // Assert
    assertNotNull(result);
    assertEquals(tmdbId, result.getId());
    assertEquals("Game of Thrones", result.getName());
    verify(tmdbTvSeries).getDetails(tmdbId, DEFAULT_LANGUAGE);
    verify(tmdbTvSeriesMapper).toTvSeriesResponse(tvSeriesDb);
  }

  @Test
  void testGetTvSeriesDetails_TmdbException_ReturnsNull() throws Exception {
    // Arrange
    Integer tmdbId = 999999;
    when(tmdbTvSeries.getDetails(tmdbId, DEFAULT_LANGUAGE))
        .thenThrow(new TmdbException("TV series not found"));

    // Act
    TmdbTvSeriesResponse result = tmdbService.getTvSeriesDetails(tmdbId);

    // Assert
    assertNull(result);
    verify(tmdbTvSeries).getDetails(tmdbId, DEFAULT_LANGUAGE);
    verify(tmdbTvSeriesMapper, never()).toTvSeriesResponse(any());
  }

  @Test
  void testGetTvSeriesDetails_GenericException_ThrowsRuntimeException() throws Exception {
    // Arrange
    Integer tmdbId = 1399;
    when(tmdbTvSeries.getDetails(tmdbId, DEFAULT_LANGUAGE))
        .thenThrow(new RuntimeException("Network error"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> tmdbService.getTvSeriesDetails(tmdbId));
    assertEquals("Failed to fetch TV series details from TMDB", exception.getMessage());
    verify(tmdbTvSeries).getDetails(tmdbId, DEFAULT_LANGUAGE);
  }

  @Test
  void testGetPopularMovies_Success() throws Exception {
    // Arrange
    String language = "en-US";
    int page = 1;
    String region = "US";

    Movie movie1 = createMockMovie(603, "The Matrix");
    Movie movie2 = createMockMovie(550, "Fight Club");

    MovieResultsPage resultsPage = mock(MovieResultsPage.class);
    when(resultsPage.getResults()).thenReturn(Arrays.asList(movie1, movie2));
    when(resultsPage.getPage()).thenReturn(1);
    when(resultsPage.getTotalResults()).thenReturn(100);

    TmdbPageResponse.TmdbMovieItem item1 = new TmdbPageResponse.TmdbMovieItem();
    item1.setId(603);
    item1.setTitle("The Matrix");

    TmdbPageResponse.TmdbMovieItem item2 = new TmdbPageResponse.TmdbMovieItem();
    item2.setId(550);
    item2.setTitle("Fight Club");

    when(tmdbMovieLists.getPopular(language, page, region)).thenReturn(resultsPage);
    when(tmdbMovieMapper.toMovieItem(movie1)).thenReturn(item1);
    when(tmdbMovieMapper.toMovieItem(movie2)).thenReturn(item2);

    // Act
    Page<TmdbPageResponse.TmdbMovieItem> result = tmdbService.getPopularMovies(language, page, region);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertEquals(100, result.getTotalElements());
    assertEquals(603, result.getContent().get(0).getId());
    assertEquals(550, result.getContent().get(1).getId());
    verify(tmdbMovieLists).getPopular(language, page, region);
  }

  @Test
  void testGetPopularMovies_NullPage_HandlesGracefully() throws Exception {
    // Arrange
    String language = "en-US";
    int page = 1;
    String region = "US";

    MovieResultsPage resultsPage = mock(MovieResultsPage.class);
    when(resultsPage.getResults()).thenReturn(Arrays.asList());
    when(resultsPage.getPage()).thenReturn(null);
    when(resultsPage.getTotalResults()).thenReturn(null);

    when(tmdbMovieLists.getPopular(language, page, region)).thenReturn(resultsPage);

    // Act
    Page<TmdbPageResponse.TmdbMovieItem> result = tmdbService.getPopularMovies(language, page, region);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getContent().size());
    assertEquals(0, result.getTotalElements());
  }

  @Test
  void testGetPopularMovies_Exception_ThrowsRuntimeException() throws Exception {
    // Arrange
    String language = "en-US";
    int page = 1;
    String region = "US";

    when(tmdbMovieLists.getPopular(language, page, region))
        .thenThrow(new RuntimeException("API error"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> tmdbService.getPopularMovies(language, page, region));
    assertEquals("Failed to fetch popular movies from TMDB", exception.getMessage());
  }

  @Test
  void testGetPopularTVShows_Success() throws Exception {
    // Arrange
    String language = "en-US";
    int page = 1;

    // Create a simple mock that will be used by the service
    // We'll verify the service calls the API and returns a result
    TvSeriesResultsPage resultsPage = mock(TvSeriesResultsPage.class);
    when(resultsPage.getResults()).thenReturn(Arrays.asList()); // Empty list for simplicity
    when(resultsPage.getPage()).thenReturn(1);
    when(resultsPage.getTotalResults()).thenReturn(0);

    when(tmdbTvSeriesLists.getPopular(language, page)).thenReturn(resultsPage);

    // Act
    Page<TmdbPageResponse.TmdbTvSeriesItem> result = tmdbService.getPopularTVShows(language, page);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getContent().size());
    assertEquals(0, result.getTotalElements());
    verify(tmdbTvSeriesLists).getPopular(language, page);
  }

  @Test
  void testGetPopularTVShows_Exception_ThrowsRuntimeException() throws Exception {
    // Arrange
    String language = "en-US";
    int page = 1;

    when(tmdbTvSeriesLists.getPopular(language, page))
        .thenThrow(new RuntimeException("API error"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> tmdbService.getPopularTVShows(language, page));
    assertEquals("Failed to fetch popular TV shows from TMDB", exception.getMessage());
  }

  @Test
  void testGetMovieGenres_Success() throws Exception {
    // Arrange
    Genre genre1 = mock(Genre.class);
    when(genre1.getId()).thenReturn(28);
    when(genre1.getName()).thenReturn("Action");

    Genre genre2 = mock(Genre.class);
    when(genre2.getId()).thenReturn(12);
    when(genre2.getName()).thenReturn("Adventure");

    when(tmdbGenre.getMovieList(DEFAULT_LANGUAGE)).thenReturn(Arrays.asList(genre1, genre2));

    // Act
    List<TmdbGenreInfo> result = tmdbService.getMovieGenres();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(28, result.get(0).getId());
    assertEquals("Action", result.get(0).getName());
    assertEquals(12, result.get(1).getId());
    assertEquals("Adventure", result.get(1).getName());
    verify(tmdbGenre).getMovieList(DEFAULT_LANGUAGE);
  }

  @Test
  void testGetMovieGenres_Exception_ThrowsRuntimeException() throws Exception {
    // Arrange
    when(tmdbGenre.getMovieList(DEFAULT_LANGUAGE))
        .thenThrow(new RuntimeException("API error"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> tmdbService.getMovieGenres());
    assertEquals("Failed to fetch movie genres from TMDB", exception.getMessage());
  }

  @Test
  void testGetTvSeriesGenres_Success() throws Exception {
    // Arrange
    Genre genre1 = mock(Genre.class);
    when(genre1.getId()).thenReturn(18);
    when(genre1.getName()).thenReturn("Drama");

    Genre genre2 = mock(Genre.class);
    when(genre2.getId()).thenReturn(10759);
    when(genre2.getName()).thenReturn("Action & Adventure");

    when(tmdbGenre.getTvList(DEFAULT_LANGUAGE)).thenReturn(Arrays.asList(genre1, genre2));

    // Act
    List<TmdbGenreInfo> result = tmdbService.getTvSeriesGenres();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(18, result.get(0).getId());
    assertEquals("Drama", result.get(0).getName());
    assertEquals(10759, result.get(1).getId());
    assertEquals("Action & Adventure", result.get(1).getName());
    verify(tmdbGenre).getTvList(DEFAULT_LANGUAGE);
  }

  @Test
  void testGetTvSeriesGenres_Exception_ThrowsRuntimeException() throws Exception {
    // Arrange
    when(tmdbGenre.getTvList(DEFAULT_LANGUAGE))
        .thenThrow(new RuntimeException("API error"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> tmdbService.getTvSeriesGenres());
    assertEquals("Failed to fetch TV series genres from TMDB", exception.getMessage());
  }

  @Test
  void testGetImageUrl_WithLeadingSlash() {
    // Arrange
    String path = "/poster.jpg";
    String size = "w500";

    // Act
    String result = tmdbService.getImageUrl(path, size);

    // Assert
    assertEquals(TEST_IMAGE_BASE_URL + "/" + size + path, result);
  }

  @Test
  void testGetImageUrl_WithoutLeadingSlash() {
    // Arrange
    String path = "poster.jpg";
    String size = "w500";

    // Act
    String result = tmdbService.getImageUrl(path, size);

    // Assert
    assertEquals(TEST_IMAGE_BASE_URL + "/" + size + "/" + path, result);
  }

  @Test
  void testGetImageUrl_NullPath_ReturnsNull() {
    // Act
    String result = tmdbService.getImageUrl(null, "w500");

    // Assert
    assertNull(result);
  }

  @Test
  void testGetImageUrl_EmptyPath_ReturnsNull() {
    // Act
    String result = tmdbService.getImageUrl("", "w500");

    // Assert
    assertNull(result);
  }

  // Helper methods
  private Movie createMockMovie(Integer id, String title) {
    Movie movie = mock(Movie.class, withSettings().lenient());
    when(movie.getId()).thenReturn(id);
    when(movie.getTitle()).thenReturn(title);
    return movie;
  }
}

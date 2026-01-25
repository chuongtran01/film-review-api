package com.filmreview.mapper;

import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.dto.tmdb.TmdbTvSeriesResponse;
import com.filmreview.entity.Title;
import com.filmreview.service.TmdbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TitleMapperTest {

  @Mock
  private TmdbService tmdbService;

  private TitleMapper titleMapper;

  private static final String TEST_IMAGE_BASE_URL = "https://image.tmdb.org/t/p";
  private static final String POSTER_PATH = "/poster.jpg";
  private static final String BACKDROP_PATH = "/backdrop.jpg";

  @BeforeEach
  void setUp() {
    titleMapper = new TitleMapper(tmdbService);
  }

  @Test
  void testToTitle_FromMovieResponse_Success() {
    // Arrange
    TmdbMovieResponse movieResponse = new TmdbMovieResponse();
    movieResponse.setId(603);
    movieResponse.setTitle("The Matrix");
    movieResponse.setOriginalTitle("The Matrix");
    movieResponse.setOverview("A computer hacker learns about the true nature of reality");
    movieResponse.setReleaseDate(LocalDate.of(1999, 3, 31));
    movieResponse.setRuntime(136);
    movieResponse.setPosterPath(POSTER_PATH);
    movieResponse.setBackdropPath(BACKDROP_PATH);
    movieResponse.setStatus("Released");
    movieResponse.setImdbId("tt0133093");

    when(tmdbService.getImageUrl(POSTER_PATH, "w500"))
        .thenReturn(TEST_IMAGE_BASE_URL + "/w500" + POSTER_PATH);
    when(tmdbService.getImageUrl(BACKDROP_PATH, "w1920"))
        .thenReturn(TEST_IMAGE_BASE_URL + "/w1920" + BACKDROP_PATH);

    // Act
    Title title = titleMapper.toTitle(movieResponse);

    // Assert
    assertNotNull(title);
    assertEquals(Title.TitleType.movie, title.getType());
    assertEquals(603, title.getTmdbId());
    assertEquals("The Matrix", title.getTitle());
    assertEquals("The Matrix", title.getOriginalTitle());
    assertEquals("thematrix", title.getSlug());
    assertEquals("A computer hacker learns about the true nature of reality", title.getSynopsis());
    assertEquals(LocalDate.of(1999, 3, 31), title.getReleaseDate());
    assertEquals(136, title.getRuntime());
    assertEquals("Released", title.getStatus());
    assertEquals("tt0133093", title.getImdbId());
    assertEquals(0, title.getUserRatingCount());
    assertNotNull(title.getPosterUrl());
    assertNotNull(title.getBackdropUrl());
    verify(tmdbService).getImageUrl(POSTER_PATH, "w500");
    verify(tmdbService).getImageUrl(BACKDROP_PATH, "w1920");
  }

  @Test
  void testToTitle_FromMovieResponse_WithoutPosterAndBackdrop() {
    // Arrange
    TmdbMovieResponse movieResponse = new TmdbMovieResponse();
    movieResponse.setId(603);
    movieResponse.setTitle("The Matrix");
    movieResponse.setOverview("A computer hacker learns about the true nature of reality");
    movieResponse.setReleaseDate(LocalDate.of(1999, 3, 31));
    movieResponse.setPosterPath(null);
    movieResponse.setBackdropPath("");

    // Act
    Title title = titleMapper.toTitle(movieResponse);

    // Assert
    assertNotNull(title);
    assertNull(title.getPosterUrl());
    assertNull(title.getBackdropUrl());
    verify(tmdbService, never()).getImageUrl(anyString(), anyString());
  }

  @Test
  void testToTitle_FromTvSeriesResponse_Success() {
    // Arrange
    TmdbTvSeriesResponse tvSeriesResponse = new TmdbTvSeriesResponse();
    tvSeriesResponse.setId(1396);
    tvSeriesResponse.setName("Breaking Bad");
    tvSeriesResponse.setOriginalName("Breaking Bad");
    tvSeriesResponse.setOverview("A high school chemistry teacher turned methamphetamine manufacturer");
    tvSeriesResponse.setFirstAirDate(LocalDate.of(2008, 1, 20));
    tvSeriesResponse.setLastAirDate(LocalDate.of(2013, 9, 29));
    tvSeriesResponse.setNumberOfSeasons(5);
    tvSeriesResponse.setNumberOfEpisodes(62);
    tvSeriesResponse.setPosterPath(POSTER_PATH);
    tvSeriesResponse.setBackdropPath(BACKDROP_PATH);
    tvSeriesResponse.setStatus("Ended");

    when(tmdbService.getImageUrl(POSTER_PATH, "w500"))
        .thenReturn(TEST_IMAGE_BASE_URL + "/w500" + POSTER_PATH);
    when(tmdbService.getImageUrl(BACKDROP_PATH, "w1920"))
        .thenReturn(TEST_IMAGE_BASE_URL + "/w1920" + BACKDROP_PATH);

    // Act
    Title title = titleMapper.toTitle(tvSeriesResponse);

    // Assert
    assertNotNull(title);
    assertEquals(Title.TitleType.tv_show, title.getType());
    assertEquals(1396, title.getTmdbId());
    assertEquals("Breaking Bad", title.getTitle());
    assertEquals("Breaking Bad", title.getOriginalTitle());
    assertEquals("breakingbad", title.getSlug());
    assertEquals("A high school chemistry teacher turned methamphetamine manufacturer", title.getSynopsis());
    assertEquals(LocalDate.of(2008, 1, 20), title.getReleaseDate());
    assertEquals(LocalDate.of(2008, 1, 20), title.getFirstAirDate());
    assertEquals(5, title.getNumberOfSeasons());
    assertEquals(62, title.getNumberOfEpisodes());
    assertEquals("Ended", title.getStatus());
    assertEquals(0, title.getUserRatingCount());
    assertNotNull(title.getPosterUrl());
    assertNotNull(title.getBackdropUrl());
    verify(tmdbService).getImageUrl(POSTER_PATH, "w500");
    verify(tmdbService).getImageUrl(BACKDROP_PATH, "w1920");
  }

  @Test
  void testToTitle_FromTvSeriesResponse_WithoutPosterAndBackdrop() {
    // Arrange
    TmdbTvSeriesResponse tvSeriesResponse = new TmdbTvSeriesResponse();
    tvSeriesResponse.setId(1396);
    tvSeriesResponse.setName("Breaking Bad");
    tvSeriesResponse.setOverview("A high school chemistry teacher turned methamphetamine manufacturer");
    tvSeriesResponse.setFirstAirDate(LocalDate.of(2008, 1, 20));
    tvSeriesResponse.setPosterPath(null);
    tvSeriesResponse.setBackdropPath("");

    // Act
    Title title = titleMapper.toTitle(tvSeriesResponse);

    // Assert
    assertNotNull(title);
    assertNull(title.getPosterUrl());
    assertNull(title.getBackdropUrl());
    verify(tmdbService, never()).getImageUrl(anyString(), anyString());
  }

  @Test
  void testToTitle_FromTvSeriesResponse_WithNullFirstAirDate() {
    // Arrange
    TmdbTvSeriesResponse tvSeriesResponse = new TmdbTvSeriesResponse();
    tvSeriesResponse.setId(1396);
    tvSeriesResponse.setName("Breaking Bad");
    tvSeriesResponse.setFirstAirDate(null);

    // Act
    Title title = titleMapper.toTitle(tvSeriesResponse);

    // Assert
    assertNotNull(title);
    assertNull(title.getReleaseDate());
    assertNull(title.getFirstAirDate());
  }

  @Test
  void testToTitle_FromTvSeriesResponse_WithNullFields() {
    // Arrange
    TmdbTvSeriesResponse tvSeriesResponse = new TmdbTvSeriesResponse();
    tvSeriesResponse.setId(1396);
    tvSeriesResponse.setName("Test Show");
    tvSeriesResponse.setOriginalName(null);
    tvSeriesResponse.setOverview(null);
    tvSeriesResponse.setFirstAirDate(null);
    tvSeriesResponse.setNumberOfSeasons(null);
    tvSeriesResponse.setNumberOfEpisodes(null);
    tvSeriesResponse.setStatus(null);

    // Act
    Title title = titleMapper.toTitle(tvSeriesResponse);

    // Assert
    assertNotNull(title);
    assertEquals(Title.TitleType.tv_show, title.getType());
    assertEquals(1396, title.getTmdbId());
    assertEquals("Test Show", title.getTitle());
    assertNull(title.getOriginalTitle());
    assertNull(title.getSynopsis());
    assertNull(title.getReleaseDate());
    assertNull(title.getFirstAirDate());
    assertNull(title.getNumberOfSeasons());
    assertNull(title.getNumberOfEpisodes());
    assertNull(title.getStatus());
    assertEquals(0, title.getUserRatingCount());
  }
}

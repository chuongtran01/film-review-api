package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbGenreInfo;
import com.filmreview.entity.Genre;
import com.filmreview.repository.GenreRepository;
import com.filmreview.util.SlugUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

  @Mock
  private GenreRepository genreRepository;

  @Mock
  private TmdbService tmdbService;

  @InjectMocks
  private GenreServiceImpl genreService;

  private TmdbGenreInfo tmdbGenre1;
  private TmdbGenreInfo tmdbGenre2;
  private Genre existingGenre;

  @BeforeEach
  void setUp() {
    tmdbGenre1 = new TmdbGenreInfo(28, "Action");
    tmdbGenre2 = new TmdbGenreInfo(12, "Adventure");

    existingGenre = new Genre();
    existingGenre.setId(28);
    existingGenre.setName("Action");
    existingGenre.setSlug("action");
    existingGenre.setCreatedAt(LocalDateTime.now());
  }

  @Test
  void testGetAllGenres_Success() {
    // Arrange
    Genre genre1 = new Genre();
    genre1.setId(28);
    genre1.setName("Action");
    genre1.setSlug("action");

    Genre genre2 = new Genre();
    genre2.setId(12);
    genre2.setName("Adventure");
    genre2.setSlug("adventure");

    List<Genre> genres = Arrays.asList(genre1, genre2);
    when(genreRepository.findAll()).thenReturn(genres);

    // Act
    List<Genre> result = genreService.getAllGenres();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(28, result.get(0).getId());
    assertEquals("Action", result.get(0).getName());
    assertEquals(12, result.get(1).getId());
    assertEquals("Adventure", result.get(1).getName());
    verify(genreRepository).findAll();
  }

  @Test
  void testGetAllGenres_EmptyList() {
    // Arrange
    when(genreRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    List<Genre> result = genreService.getAllGenres();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(genreRepository).findAll();
  }

  @Test
  void testSyncMovieGenres_Success_NewGenres() {
    // Arrange
    List<TmdbGenreInfo> tmdbGenres = Arrays.asList(tmdbGenre1, tmdbGenre2);
    when(tmdbService.getMovieGenres()).thenReturn(tmdbGenres);
    when(genreRepository.findById(28)).thenReturn(Optional.empty());
    when(genreRepository.findById(12)).thenReturn(Optional.empty());

    Genre savedGenre1 = new Genre();
    savedGenre1.setId(28);
    savedGenre1.setName("Action");
    savedGenre1.setSlug(SlugUtils.generateSlug("Action", 100));

    Genre savedGenre2 = new Genre();
    savedGenre2.setId(12);
    savedGenre2.setName("Adventure");
    savedGenre2.setSlug(SlugUtils.generateSlug("Adventure", 100));

    when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> {
      Genre genre = invocation.getArgument(0);
      if (genre.getCreatedAt() == null) {
        genre.setCreatedAt(LocalDateTime.now());
      }
      return genre;
    });

    // Act
    int result = genreService.syncMovieGenres();

    // Assert
    assertEquals(2, result);
    verify(tmdbService).getMovieGenres();
    verify(genreRepository, times(2)).findById(anyInt());
    verify(genreRepository, times(2)).save(any(Genre.class));
  }

  @Test
  void testSyncMovieGenres_Success_ExistingGenres_NoUpdate() {
    // Arrange
    List<TmdbGenreInfo> tmdbGenres = Arrays.asList(tmdbGenre1);
    when(tmdbService.getMovieGenres()).thenReturn(tmdbGenres);
    when(genreRepository.findById(28)).thenReturn(Optional.of(existingGenre));

    // Act
    int result = genreService.syncMovieGenres();

    // Assert
    assertEquals(0, result); // No updates needed
    verify(tmdbService).getMovieGenres();
    verify(genreRepository).findById(28);
    verify(genreRepository, never()).save(any(Genre.class));
  }

  @Test
  void testSyncMovieGenres_Success_ExistingGenres_WithUpdate() {
    // Arrange
    TmdbGenreInfo updatedGenre = new TmdbGenreInfo(28, "Action Updated");
    List<TmdbGenreInfo> tmdbGenres = Arrays.asList(updatedGenre);
    when(tmdbService.getMovieGenres()).thenReturn(tmdbGenres);
    when(genreRepository.findById(28)).thenReturn(Optional.of(existingGenre));

    Genre updated = new Genre();
    updated.setId(28);
    updated.setName("Action Updated");
    updated.setSlug(SlugUtils.generateSlug("Action Updated", 100));
    updated.setCreatedAt(existingGenre.getCreatedAt());

    when(genreRepository.save(any(Genre.class))).thenReturn(updated);

    // Act
    int result = genreService.syncMovieGenres();

    // Assert
    assertEquals(1, result); // One update
    verify(tmdbService).getMovieGenres();
    verify(genreRepository).findById(28);
    verify(genreRepository).save(any(Genre.class));
  }

  @Test
  void testSyncMovieGenres_Success_NewGenre_NoCreatedAt() {
    // Arrange
    List<TmdbGenreInfo> tmdbGenres = Arrays.asList(tmdbGenre1);
    when(tmdbService.getMovieGenres()).thenReturn(tmdbGenres);

    Genre newGenre = new Genre();
    newGenre.setId(28);
    newGenre.setName("Action");
    when(genreRepository.findById(28)).thenReturn(Optional.of(newGenre));

    Genre savedGenre = new Genre();
    savedGenre.setId(28);
    savedGenre.setName("Action");
    savedGenre.setSlug(SlugUtils.generateSlug("Action", 100));
    savedGenre.setCreatedAt(LocalDateTime.now());

    when(genreRepository.save(any(Genre.class))).thenReturn(savedGenre);

    // Act
    int result = genreService.syncMovieGenres();

    // Assert
    assertEquals(1, result); // Saved because createdAt was null
    verify(tmdbService).getMovieGenres();
    verify(genreRepository).findById(28);
    verify(genreRepository).save(any(Genre.class));
  }

  @Test
  void testSyncMovieGenres_EmptyList() {
    // Arrange
    when(tmdbService.getMovieGenres()).thenReturn(Collections.emptyList());

    // Act
    int result = genreService.syncMovieGenres();

    // Assert
    assertEquals(0, result);
    verify(tmdbService).getMovieGenres();
    verify(genreRepository, never()).findById(anyInt());
    verify(genreRepository, never()).save(any(Genre.class));
  }

  @Test
  void testSyncMovieGenres_NullList() {
    // Arrange
    when(tmdbService.getMovieGenres()).thenReturn(null);

    // Act
    int result = genreService.syncMovieGenres();

    // Assert
    assertEquals(0, result);
    verify(tmdbService).getMovieGenres();
    verify(genreRepository, never()).findById(anyInt());
    verify(genreRepository, never()).save(any(Genre.class));
  }

  @Test
  void testSyncTvSeriesGenres_Success_NewGenres() {
    // Arrange
    List<TmdbGenreInfo> tmdbGenres = Arrays.asList(tmdbGenre1, tmdbGenre2);
    when(tmdbService.getTvSeriesGenres()).thenReturn(tmdbGenres);
    when(genreRepository.findById(28)).thenReturn(Optional.empty());
    when(genreRepository.findById(12)).thenReturn(Optional.empty());

    when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> {
      Genre genre = invocation.getArgument(0);
      if (genre.getCreatedAt() == null) {
        genre.setCreatedAt(LocalDateTime.now());
      }
      return genre;
    });

    // Act
    int result = genreService.syncTvSeriesGenres();

    // Assert
    assertEquals(2, result);
    verify(tmdbService).getTvSeriesGenres();
    verify(genreRepository, times(2)).findById(anyInt());
    verify(genreRepository, times(2)).save(any(Genre.class));
  }

  @Test
  void testSyncTvSeriesGenres_Success_ExistingGenres_NoUpdate() {
    // Arrange
    List<TmdbGenreInfo> tmdbGenres = Arrays.asList(tmdbGenre1);
    when(tmdbService.getTvSeriesGenres()).thenReturn(tmdbGenres);
    when(genreRepository.findById(28)).thenReturn(Optional.of(existingGenre));

    // Act
    int result = genreService.syncTvSeriesGenres();

    // Assert
    assertEquals(0, result); // No updates needed
    verify(tmdbService).getTvSeriesGenres();
    verify(genreRepository).findById(28);
    verify(genreRepository, never()).save(any(Genre.class));
  }

  @Test
  void testSyncTvSeriesGenres_Success_ExistingGenres_WithUpdate() {
    // Arrange
    TmdbGenreInfo updatedGenre = new TmdbGenreInfo(28, "Action Updated");
    List<TmdbGenreInfo> tmdbGenres = Arrays.asList(updatedGenre);
    when(tmdbService.getTvSeriesGenres()).thenReturn(tmdbGenres);
    when(genreRepository.findById(28)).thenReturn(Optional.of(existingGenre));

    Genre updated = new Genre();
    updated.setId(28);
    updated.setName("Action Updated");
    updated.setSlug(SlugUtils.generateSlug("Action Updated", 100));
    updated.setCreatedAt(existingGenre.getCreatedAt());

    when(genreRepository.save(any(Genre.class))).thenReturn(updated);

    // Act
    int result = genreService.syncTvSeriesGenres();

    // Assert
    assertEquals(1, result); // One update
    verify(tmdbService).getTvSeriesGenres();
    verify(genreRepository).findById(28);
    verify(genreRepository).save(any(Genre.class));
  }

  @Test
  void testSyncTvSeriesGenres_EmptyList() {
    // Arrange
    when(tmdbService.getTvSeriesGenres()).thenReturn(Collections.emptyList());

    // Act
    int result = genreService.syncTvSeriesGenres();

    // Assert
    assertEquals(0, result);
    verify(tmdbService).getTvSeriesGenres();
    verify(genreRepository, never()).findById(anyInt());
    verify(genreRepository, never()).save(any(Genre.class));
  }

  @Test
  void testSyncTvSeriesGenres_NullList() {
    // Arrange
    when(tmdbService.getTvSeriesGenres()).thenReturn(null);

    // Act
    int result = genreService.syncTvSeriesGenres();

    // Assert
    assertEquals(0, result);
    verify(tmdbService).getTvSeriesGenres();
    verify(genreRepository, never()).findById(anyInt());
    verify(genreRepository, never()).save(any(Genre.class));
  }

  @Test
  void testSyncMovieGenres_SlugUpdate() {
    // Arrange
    Genre genreWithOldSlug = new Genre();
    genreWithOldSlug.setId(28);
    genreWithOldSlug.setName("Action");
    genreWithOldSlug.setSlug("old-action-slug");
    genreWithOldSlug.setCreatedAt(LocalDateTime.now());

    List<TmdbGenreInfo> tmdbGenres = Arrays.asList(tmdbGenre1);
    when(tmdbService.getMovieGenres()).thenReturn(tmdbGenres);
    when(genreRepository.findById(28)).thenReturn(Optional.of(genreWithOldSlug));

    Genre updated = new Genre();
    updated.setId(28);
    updated.setName("Action");
    updated.setSlug(SlugUtils.generateSlug("Action", 100));
    updated.setCreatedAt(genreWithOldSlug.getCreatedAt());

    when(genreRepository.save(any(Genre.class))).thenReturn(updated);

    // Act
    int result = genreService.syncMovieGenres();

    // Assert
    assertEquals(1, result); // Updated because slug changed
    verify(genreRepository).save(any(Genre.class));
  }
}

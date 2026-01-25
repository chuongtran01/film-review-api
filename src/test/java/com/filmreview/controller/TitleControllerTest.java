package com.filmreview.controller;

import com.filmreview.entity.Title;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TitleControllerTest {

  @Mock
  private TitleService titleService;

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
    when(titleService.getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class)))
        .thenReturn(moviePage);

    // Act
    ResponseEntity<Page<Title>> response = titleController.getTitles(
        null, "movie", null, null, null, 1, 20, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getTotalElements());
    assertEquals(1, response.getBody().getContent().size());
    assertEquals("The Matrix", response.getBody().getContent().get(0).getTitle());
    verify(titleService).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleService, never()).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
  }

  @Test
  void testGetTitles_PopularTVShows() {
    // Arrange
    Page<Title> tvPage = new PageImpl<>(List.of(testTVShow), pageable, 1);
    when(titleService.getPopularTVShows(anyString(), anyInt(), any(Pageable.class)))
        .thenReturn(tvPage);

    // Act
    ResponseEntity<Page<Title>> response = titleController.getTitles(
        null, "tv_show", null, null, null, 1, 20, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getTotalElements());
    assertEquals(1, response.getBody().getContent().size());
    assertEquals("Breaking Bad", response.getBody().getContent().get(0).getTitle());
    verify(titleService).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
    verify(titleService, never()).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
  }

  @Test
  void testGetTitles_PopularWithoutType_ReturnsEmpty() {
    // Act
    ResponseEntity<Page<Title>> response = titleController.getTitles(
        null, null, null, null, null, 1, 20, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getTotalElements());
    assertTrue(response.getBody().getContent().isEmpty());
    verify(titleService, never()).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleService, never()).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
  }

  @Test
  void testGetTitles_PopularWithInvalidType_ReturnsEmpty() {
    // Act
    ResponseEntity<Page<Title>> response = titleController.getTitles(
        null, "invalid_type", null, null, null, 1, 20, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getTotalElements());
    verify(titleService, never()).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleService, never()).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
  }

  @Test
  void testGetTitles_NonPopularSort_ReturnsEmpty() {
    // Act
    ResponseEntity<Page<Title>> response = titleController.getTitles(
        null, "movie", null, null, null, 1, 20, "top_rated");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().getTotalElements());
    verify(titleService, never()).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
    verify(titleService, never()).getPopularTVShows(anyString(), anyInt(), any(Pageable.class));
  }

  @Test
  void testGetTitles_Pagination() {
    // Arrange
    Page<Title> moviePage = new PageImpl<>(List.of(testMovie), PageRequest.of(1, 10), 25);
    when(titleService.getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class)))
        .thenReturn(moviePage);

    // Act
    ResponseEntity<Page<Title>> response = titleController.getTitles(
        null, "movie", null, null, null, 2, 10, "popular");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(25, response.getBody().getTotalElements());
    assertEquals(1, response.getBody().getContent().size());
    verify(titleService).getPopularMovies(anyString(), anyInt(), anyString(), any(Pageable.class));
  }

  @Test
  void testGetTitleById_NotImplemented() {
    // Act
    ResponseEntity<Title> response = titleController.getTitleById("test-id");

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(titleService, never()).getTitleById(any());
  }
}

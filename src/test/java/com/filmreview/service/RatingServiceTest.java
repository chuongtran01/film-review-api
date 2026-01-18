package com.filmreview.service;

import com.filmreview.dto.RatingRequest;
import com.filmreview.dto.RatingResponse;
import com.filmreview.entity.User;
import com.filmreview.exception.BadRequestException;
import com.filmreview.exception.NotFoundException;
import com.filmreview.repository.RatingRepository;
import com.filmreview.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
@Transactional
class RatingServiceTest {

  @Autowired
  private RatingService ratingService;

  @Autowired
  private RatingRepository ratingRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private User testUser;
  private UUID titleId;

  @BeforeEach
  void setUp() {
    ratingRepository.deleteAll();
    userRepository.deleteAll();
    jdbcTemplate.update("DELETE FROM titles");

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setUsername("testuser");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);

    // Create test title
    titleId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId, 1, "Test Movie", "test-movie");
  }

  @Test
  void testCreateOrUpdateRating_CreateNew() {
    RatingRequest request = new RatingRequest();
    request.setScore(8);

    RatingResponse response = ratingService.createOrUpdateRating(testUser.getId(), titleId, request);

    assertNotNull(response.getId());
    assertEquals(8, response.getScore());
    assertEquals(testUser.getId(), response.getUserId());
    assertEquals(titleId, response.getTitleId());
    assertNotNull(response.getCreatedAt());
  }

  @Test
  void testCreateOrUpdateRating_UpdateExisting() {
    // Create initial rating
    RatingRequest request1 = new RatingRequest();
    request1.setScore(8);
    RatingResponse response1 = ratingService.createOrUpdateRating(testUser.getId(), titleId, request1);
    UUID ratingId = response1.getId();

    // Update rating
    RatingRequest request2 = new RatingRequest();
    request2.setScore(9);
    RatingResponse response2 = ratingService.createOrUpdateRating(testUser.getId(), titleId, request2);

    // Should be same rating ID
    assertEquals(ratingId, response2.getId());
    assertEquals(9, response2.getScore());
    // Updated timestamp should be different or equal
    assertNotNull(response2.getUpdatedAt());
  }

  @Test
  void testCreateOrUpdateRating_InvalidScore_TooLow() {
    RatingRequest request = new RatingRequest();
    request.setScore(0);

    assertThrows(BadRequestException.class, () ->
        ratingService.createOrUpdateRating(testUser.getId(), titleId, request));
  }

  @Test
  void testCreateOrUpdateRating_InvalidScore_TooHigh() {
    RatingRequest request = new RatingRequest();
    request.setScore(11);

    assertThrows(BadRequestException.class, () ->
        ratingService.createOrUpdateRating(testUser.getId(), titleId, request));
  }

  @Test
  void testCreateOrUpdateRating_InvalidScore_Null() {
    RatingRequest request = new RatingRequest();
    request.setScore(null);

    assertThrows(BadRequestException.class, () ->
        ratingService.createOrUpdateRating(testUser.getId(), titleId, request));
  }

  @Test
  void testDeleteRating_Success() {
    // Create rating first
    RatingRequest request = new RatingRequest();
    request.setScore(8);
    ratingService.createOrUpdateRating(testUser.getId(), titleId, request);

    // Delete rating
    assertDoesNotThrow(() -> ratingService.deleteRating(testUser.getId(), titleId));

    // Verify deleted
    assertFalse(ratingRepository.existsByUserIdAndTitleId(testUser.getId(), titleId));
  }

  @Test
  void testDeleteRating_NotFound() {
    assertThrows(NotFoundException.class, () ->
        ratingService.deleteRating(testUser.getId(), titleId));
  }

  @Test
  void testGetUserRatings() {
    // Create multiple titles and ratings
    UUID titleId2 = UUID.randomUUID();
    UUID titleId3 = UUID.randomUUID();

    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId2, 2, "Test Movie 2", "test-movie-2");
    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId3, 3, "Test Movie 3", "test-movie-3");

    ratingService.createOrUpdateRating(testUser.getId(), titleId, new RatingRequest(8));
    ratingService.createOrUpdateRating(testUser.getId(), titleId2, new RatingRequest(9));
    ratingService.createOrUpdateRating(testUser.getId(), titleId3, new RatingRequest(10));

    Pageable pageable = PageRequest.of(0, 10);
    Page<RatingResponse> ratings = ratingService.getUserRatings(testUser.getId(), pageable);

    assertEquals(3, ratings.getTotalElements());
    assertEquals(3, ratings.getContent().size());
  }

  @Test
  void testGetUserRatings_Pagination() {
    // Create 5 titles and ratings
    for (int i = 0; i < 5; i++) {
      UUID testTitleId = UUID.randomUUID();
      jdbcTemplate.update(
          "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
          testTitleId, 10 + i, "Test Movie " + i, "test-movie-" + i);
      ratingService.createOrUpdateRating(testUser.getId(), testTitleId, new RatingRequest(8));
    }

    Pageable pageable = PageRequest.of(0, 2);
    Page<RatingResponse> ratings = ratingService.getUserRatings(testUser.getId(), pageable);

    assertEquals(5, ratings.getTotalElements());
    assertEquals(2, ratings.getContent().size());
    assertTrue(ratings.hasNext());
  }

  @Test
  void testGetRating_Success() {
    RatingRequest request = new RatingRequest();
    request.setScore(8);
    RatingResponse created = ratingService.createOrUpdateRating(testUser.getId(), titleId, request);

    RatingResponse found = ratingService.getRating(testUser.getId(), titleId);

    assertNotNull(found);
    assertEquals(created.getId(), found.getId());
    assertEquals(8, found.getScore());
  }

  @Test
  void testGetRating_NotFound() {
    assertThrows(NotFoundException.class, () ->
        ratingService.getRating(testUser.getId(), titleId));
  }

  @Test
  void testGetTitleRatings() {
    // Create ratings from multiple users
    User user2 = new User();
    user2.setEmail("user2@example.com");
    user2.setUsername("user2");
    user2.setPasswordHash(passwordEncoder.encode("password123"));
    user2 = userRepository.save(user2);

    ratingService.createOrUpdateRating(testUser.getId(), titleId, new RatingRequest(8));
    ratingService.createOrUpdateRating(user2.getId(), titleId, new RatingRequest(9));

    Pageable pageable = PageRequest.of(0, 10);
    Page<RatingResponse> ratings = ratingService.getTitleRatings(titleId, pageable);

    assertEquals(2, ratings.getTotalElements());
    assertEquals(2, ratings.getContent().size());
  }

  @Test
  void testGetTitleRatings_Empty() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<RatingResponse> ratings = ratingService.getTitleRatings(titleId, pageable);

    assertEquals(0, ratings.getTotalElements());
    assertTrue(ratings.getContent().isEmpty());
  }
}

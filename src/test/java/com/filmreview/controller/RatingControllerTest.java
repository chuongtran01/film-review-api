package com.filmreview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmreview.dto.RatingRequest;
import com.filmreview.entity.Rating;
import com.filmreview.entity.User;
import com.filmreview.faker.RatingFaker;
import com.filmreview.faker.UserFaker;
import com.filmreview.repository.RatingRepository;
import com.filmreview.repository.UserRepository;
import com.filmreview.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "dev", "test" })
@Transactional
class RatingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RatingRepository ratingRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private User testUser;
  private String accessToken;
  private UUID titleId;

  @BeforeEach
  void setUp() {
    // Create test user using faker
    testUser = UserFaker.generate("test@example.com", "testuser");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);

    // Generate JWT token
    List<String> roles = List.of("USER");
    List<String> permissions = List.of();
    accessToken = tokenProvider.generateAccessToken(
        testUser.getId(),
        testUser.getUsername(),
        testUser.getEmail(),
        roles,
        permissions);

    // Create test title using JDBC (no Title entity/repository)
    titleId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId, 1, "Test Movie", "test-movie");
  }

  private UUID createTitle(UUID titleId, Integer tmdbId, String title, String slug) {
    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId, tmdbId, title, slug);
    return titleId;
  }

  @Test
  void testCreateOrUpdateRating_Success() throws Exception {
    RatingRequest request = new RatingRequest();
    request.setScore(8);

    mockMvc.perform(put("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(8))
        .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.titleId").value(titleId.toString()))
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  void testCreateOrUpdateRating_UpdateExisting() throws Exception {
    // Create initial rating
    RatingRequest request1 = new RatingRequest();
    request1.setScore(8);
    mockMvc.perform(put("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request1)));

    // Update rating
    RatingRequest request2 = new RatingRequest();
    request2.setScore(9);

    mockMvc.perform(put("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(9));
  }

  @Test
  void testCreateOrUpdateRating_InvalidScore_TooLow() throws Exception {
    RatingRequest request = new RatingRequest();
    request.setScore(0);

    mockMvc.perform(put("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateOrUpdateRating_InvalidScore_TooHigh() throws Exception {
    RatingRequest request = new RatingRequest();
    request.setScore(11);

    mockMvc.perform(put("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateOrUpdateRating_Unauthorized() throws Exception {
    RatingRequest request = new RatingRequest();
    request.setScore(8);

    mockMvc.perform(put("/api/v1/ratings/{titleId}", titleId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized()); // 401 - AuthenticationEntryPoint returns Unauthorized when not authenticated
  }

  @Test
  void testDeleteRating_Success() throws Exception {
    // Create rating first
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    ratingRepository.save(rating);

    mockMvc.perform(delete("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    // Verify deleted
    assertFalse(ratingRepository.existsByUserIdAndTitleId(testUser.getId(), titleId));
  }

  @Test
  void testDeleteRating_NotFound() throws Exception {
    mockMvc.perform(delete("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetRating_Success() throws Exception {
    // Create rating first
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    ratingRepository.save(rating);

    mockMvc.perform(get("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(8))
        .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.titleId").value(titleId.toString()));
  }

  @Test
  void testGetRating_NotFound() throws Exception {
    mockMvc.perform(get("/api/v1/ratings/{titleId}", titleId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetUserRatings() throws Exception {
    // Create multiple titles and ratings
    UUID titleId2 = createTitle(UUID.randomUUID(), 2, "Test Movie 2", "test-movie-2");
    UUID titleId3 = createTitle(UUID.randomUUID(), 3, "Test Movie 3", "test-movie-3");

    Rating rating1 = RatingFaker.generate(testUser.getId(), titleId, 8);
    ratingRepository.save(rating1);

    Rating rating2 = RatingFaker.generate(testUser.getId(), titleId2, 9);
    ratingRepository.save(rating2);

    Rating rating3 = RatingFaker.generate(testUser.getId(), titleId3, 10);
    ratingRepository.save(rating3);

    mockMvc.perform(get("/api/v1/ratings")
        .header("Authorization", "Bearer " + accessToken)
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.totalElements").value(3));
  }

  @Test
  void testGetUserRatings_Pagination() throws Exception {
    // Create 5 titles and ratings
    for (int i = 0; i < 5; i++) {
      UUID testTitleId = createTitle(UUID.randomUUID(), 10 + i, "Test Movie " + i, "test-movie-" + i);
      Rating rating = RatingFaker.generate(testUser.getId(), testTitleId, 8);
      ratingRepository.save(rating);
    }

    mockMvc.perform(get("/api/v1/ratings")
        .header("Authorization", "Bearer " + accessToken)
        .param("page", "0")
        .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.totalPages").value(3));
  }

  @Test
  void testGetTitleRatings_Public() throws Exception {
    // Create ratings from multiple users
    User user2 = UserFaker.generate("user2@example.com", "user2");
    user2.setPasswordHash(passwordEncoder.encode("password123"));
    user2 = userRepository.save(user2);

    // Title already created in setUp()
    Rating rating1 = RatingFaker.generate(testUser.getId(), titleId, 8);
    ratingRepository.save(rating1);

    Rating rating2 = RatingFaker.generate(user2.getId(), titleId, 9);
    ratingRepository.save(rating2);

    // Public endpoint - no auth required
    mockMvc.perform(get("/api/v1/ratings/titles/{titleId}", titleId)
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  void testGetTitleRatings_Empty() throws Exception {
    mockMvc.perform(get("/api/v1/ratings/titles/{titleId}", titleId)
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0));
  }
}

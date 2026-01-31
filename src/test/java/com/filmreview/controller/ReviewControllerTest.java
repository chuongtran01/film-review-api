package com.filmreview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmreview.dto.ReviewRequest;
import com.filmreview.dto.ReviewUpdateRequest;
import com.filmreview.entity.Rating;
import com.filmreview.entity.Review;
import com.filmreview.entity.User;
import com.filmreview.faker.RatingFaker;
import com.filmreview.faker.ReviewFaker;
import com.filmreview.faker.UserFaker;
import com.filmreview.repository.RatingRepository;
import com.filmreview.repository.ReviewHelpfulRepository;
import com.filmreview.repository.ReviewRepository;
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
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private RatingRepository ratingRepository;

  @Autowired
  private ReviewHelpfulRepository reviewHelpfulRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private User testUser;
  private User otherUser;
  private String accessToken;
  private String otherUserAccessToken;
  private UUID titleId;

  @BeforeEach
  void setUp() {
    // Create test user
    testUser = UserFaker.generate("test@example.com", "testuser");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);

    // Create other user
    otherUser = UserFaker.generate("other@example.com", "otheruser");
    otherUser.setPasswordHash(passwordEncoder.encode("password123"));
    otherUser = userRepository.save(otherUser);

    // Generate JWT tokens
    List<String> roles = List.of("USER");
    List<String> permissions = List.of();
    accessToken = tokenProvider.generateAccessToken(
        testUser.getId(),
        testUser.getUsername(),
        testUser.getEmail(),
        roles,
        permissions);
    otherUserAccessToken = tokenProvider.generateAccessToken(
        otherUser.getId(),
        otherUser.getUsername(),
        otherUser.getEmail(),
        roles,
        permissions);

    // Create test title
    titleId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId, 12345, "Test Movie", "test-movie");
  }

  @Test
  void testCreateReview_Success() throws Exception {
    // Arrange
    ReviewRequest request = new ReviewRequest();
    request.setTitleId(titleId);
    request.setTitle("Amazing Film Review");
    request.setContent(
        "This movie exceeded all my expectations. The cinematography was stunning and the performances were outstanding throughout the entire film.");
    request.setRatingScore(9);
    request.setContainsSpoilers(false);

    // Act & Assert
    mockMvc.perform(post("/api/v1/reviews")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reviewTitle").value("Amazing Film Review"))
        .andExpect(jsonPath("$.content").exists())
        .andExpect(jsonPath("$.rating.score").value(9))
        .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.titleId").value(titleId.toString()));

    // Verify review was created
    List<Review> reviews = reviewRepository.findByUserIdAndTitleId(testUser.getId(), titleId)
        .stream().toList();
    assertEquals(1, reviews.size());
  }

  @Test
  void testCreateReview_Unauthorized() throws Exception {
    // Arrange
    ReviewRequest request = new ReviewRequest();
    request.setTitleId(titleId);
    request.setTitle("Test Review");
    request.setContent("This is a test review with enough content to meet the minimum requirement of 50 characters.");
    request.setRatingScore(8);

    // Act & Assert
    mockMvc.perform(post("/api/v1/reviews")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testCreateReview_InvalidRequest() throws Exception {
    // Arrange - Missing required fields
    ReviewRequest request = new ReviewRequest();
    request.setTitleId(titleId);
    // Missing title, content, and ratingScore

    // Act & Assert
    mockMvc.perform(post("/api/v1/reviews")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateReview_AlreadyExists() throws Exception {
    // Arrange - Create existing review
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review existingReview = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Existing Review",
        "This is an existing review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    reviewRepository.save(existingReview);

    ReviewRequest request = new ReviewRequest();
    request.setTitleId(titleId);
    request.setTitle("New Review");
    request.setContent("This is a new review with enough content to meet the minimum requirement of 50 characters.");
    request.setRatingScore(7);

    // Act & Assert
    mockMvc.perform(post("/api/v1/reviews")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGetReviewById_Success() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 5, null, null, null);
    review = reviewRepository.save(review);

    // Act & Assert
    mockMvc.perform(get("/api/v1/reviews/{id}", review.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(review.getId().toString()))
        .andExpect(jsonPath("$.reviewTitle").value("Test Review"))
        .andExpect(jsonPath("$.userId").value(testUser.getId().toString()));
  }

  @Test
  void testGetReviewById_NotFound() throws Exception {
    // Arrange
    UUID nonExistentId = UUID.randomUUID();

    // Act & Assert
    mockMvc.perform(get("/api/v1/reviews/{id}", nonExistentId))
        .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateReview_Success() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Original Review",
        "This is the original review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setTitle("Updated Review Title");
    request
        .setContent("This is an updated review with enough content to meet the minimum requirement of 50 characters.");
    request.setContainsSpoilers(true);

    // Act & Assert
    mockMvc.perform(patch("/api/v1/reviews/{id}", review.getId())
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviewTitle").value("Updated Review Title"))
        .andExpect(jsonPath("$.containsSpoilers").value(true));
  }

  @Test
  void testUpdateReview_Unauthorized() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setTitle("Updated Title");

    // Act & Assert
    mockMvc.perform(patch("/api/v1/reviews/{id}", review.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testUpdateReview_Forbidden() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setTitle("Updated Title");

    // Act & Assert - Other user trying to update
    mockMvc.perform(patch("/api/v1/reviews/{id}", review.getId())
        .header("Authorization", "Bearer " + otherUserAccessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUpdateReview_UpdateRating() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setRatingScore(10);

    // Act & Assert
    mockMvc.perform(patch("/api/v1/reviews/{id}", review.getId())
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rating.score").value(10));
  }

  @Test
  void testDeleteReview_Success() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    // Act & Assert
    mockMvc.perform(delete("/api/v1/reviews/{id}", review.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    // Verify review is soft-deleted
    Review deletedReview = reviewRepository.findById(review.getId()).orElse(null);
    assertNotNull(deletedReview);
    assertNotNull(deletedReview.getDeletedAt());

    // Verify rating is deleted
    assertFalse(ratingRepository.findById(rating.getId()).isPresent());
  }

  @Test
  void testDeleteReview_Forbidden() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    // Act & Assert - Other user trying to delete
    mockMvc.perform(delete("/api/v1/reviews/{id}", review.getId())
        .header("Authorization", "Bearer " + otherUserAccessToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void testGetUserReviews_Success() throws Exception {
    // Arrange
    Rating rating1 = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating1 = ratingRepository.save(rating1);

    Review review1 = ReviewFaker.generate(null, testUser.getId(), titleId, rating1.getId(),
        "Review 1",
        "This is the first review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    reviewRepository.save(review1);

    // Create another title and review
    UUID titleId2 = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId2, 12346, "Test Movie 2", "test-movie-2");

    Rating rating2 = RatingFaker.generate(testUser.getId(), titleId2, 9);
    rating2 = ratingRepository.save(rating2);

    Review review2 = ReviewFaker.generate(null, testUser.getId(), titleId2, rating2.getId(),
        "Review 2",
        "This is the second review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    reviewRepository.save(review2);

    // Act & Assert
    mockMvc.perform(get("/api/v1/reviews")
        .header("Authorization", "Bearer " + accessToken)
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  void testGetTitleReviews_Success() throws Exception {
    // Arrange
    Rating rating1 = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating1 = ratingRepository.save(rating1);

    Review review1 = ReviewFaker.generate(null, testUser.getId(), titleId, rating1.getId(),
        "Review 1",
        "This is the first review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    reviewRepository.save(review1);

    Rating rating2 = RatingFaker.generate(otherUser.getId(), titleId, 7);
    rating2 = ratingRepository.save(rating2);

    Review review2 = ReviewFaker.generate(null, otherUser.getId(), titleId, rating2.getId(),
        "Review 2",
        "This is the second review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    reviewRepository.save(review2);

    // Act & Assert
    mockMvc.perform(get("/api/v1/reviews/titles/{titleId}", titleId)
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  void testGetTitleReviews_WithSort_Newest() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    reviewRepository.save(review);

    // Act & Assert
    mockMvc.perform(get("/api/v1/reviews/titles/{titleId}", titleId)
        .param("sort", "newest")
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void testGetTitleReviews_WithSort_Helpful() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 10, null, null, null);
    reviewRepository.save(review);

    // Act & Assert
    mockMvc.perform(get("/api/v1/reviews/titles/{titleId}", titleId)
        .param("sort", "helpful")
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void testGetUserReviewForTitle_Success() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "Test Review",
        "This is a test review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    // Act & Assert
    mockMvc.perform(get("/api/v1/reviews/titles/{titleId}/me", titleId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(review.getId().toString()))
        .andExpect(jsonPath("$.userId").value(testUser.getId().toString()));
  }

  @Test
  void testGetUserReviewForTitle_NotFound() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/v1/reviews/titles/{titleId}/me", titleId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void testMarkHelpful_Success() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(otherUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, otherUser.getId(), titleId, rating.getId(),
        "Other User Review",
        "This is a review by another user with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    // Act & Assert
    mockMvc.perform(post("/api/v1/reviews/{id}/helpful", review.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());

    // Verify helpful vote was created
    assertTrue(reviewHelpfulRepository.existsByUserIdAndReviewId(testUser.getId(), review.getId()));
  }

  @Test
  void testMarkHelpful_SelfLike() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(testUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, testUser.getId(), titleId, rating.getId(),
        "My Review",
        "This is my own review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    // Act & Assert
    mockMvc.perform(post("/api/v1/reviews/{id}/helpful", review.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testMarkHelpful_AlreadyMarked() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(otherUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, otherUser.getId(), titleId, rating.getId(),
        "Other User Review",
        "This is a review by another user with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    // Mark as helpful first time
    mockMvc.perform(post("/api/v1/reviews/{id}/helpful", review.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());

    // Try to mark again
    mockMvc.perform(post("/api/v1/reviews/{id}/helpful", review.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testUnmarkHelpful_Success() throws Exception {
    // Arrange
    Rating rating = RatingFaker.generate(otherUser.getId(), titleId, 8);
    rating = ratingRepository.save(rating);

    Review review = ReviewFaker.generate(null, otherUser.getId(), titleId, rating.getId(),
        "Other User Review",
        "This is a review by another user with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);
    review = reviewRepository.save(review);

    // Mark as helpful first
    mockMvc.perform(post("/api/v1/reviews/{id}/helpful", review.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());

    // Act & Assert - Unmark helpful
    mockMvc.perform(delete("/api/v1/reviews/{id}/helpful", review.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());

    // Verify helpful vote was removed
    assertFalse(reviewHelpfulRepository.existsByUserIdAndReviewId(testUser.getId(), review.getId()));
  }

  @Test
  void testUnmarkHelpful_NotFound() throws Exception {
    // Arrange
    UUID nonExistentId = UUID.randomUUID();

    // Act & Assert
    mockMvc.perform(delete("/api/v1/reviews/{id}/helpful", nonExistentId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound());
  }
}

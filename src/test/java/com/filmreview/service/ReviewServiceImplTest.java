package com.filmreview.service;

import com.filmreview.dto.ReviewRequest;
import com.filmreview.dto.ReviewResponse;
import com.filmreview.dto.ReviewUpdateRequest;
import com.filmreview.dto.TitleDto;
import com.filmreview.entity.Rating;
import com.filmreview.entity.Review;
import com.filmreview.entity.ReviewHelpful;
import com.filmreview.entity.Title;
import com.filmreview.entity.User;
import com.filmreview.exception.BadRequestException;
import com.filmreview.exception.ForbiddenException;
import com.filmreview.exception.NotFoundException;
import com.filmreview.faker.RatingFaker;
import com.filmreview.faker.ReviewFaker;
import com.filmreview.mapper.TitleDtoMapper;
import com.filmreview.repository.RatingRepository;
import com.filmreview.repository.ReviewHelpfulRepository;
import com.filmreview.repository.ReviewRepository;
import com.filmreview.repository.TitleRepository;
import com.filmreview.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private ReviewHelpfulRepository reviewHelpfulRepository;

  @Mock
  private RatingRepository ratingRepository;

  @Mock
  private TitleRepository titleRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TitleDtoMapper titleDtoMapper;

  @InjectMocks
  private ReviewServiceImpl reviewService;

  private UUID userId;
  private UUID titleId;
  private UUID reviewId;
  private UUID ratingId;
  private Title testTitle;
  private User testUser;
  private Rating testRating;
  private Review testReview;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    titleId = UUID.randomUUID();
    reviewId = UUID.randomUUID();
    ratingId = UUID.randomUUID();

    testTitle = new Title();
    testTitle.setId(titleId);
    testTitle.setTitle("Test Movie");
    testTitle.setTmdbId(12345);
    testTitle.setType(Title.TitleType.movie);

    testUser = new User();
    testUser.setId(userId);
    testUser.setUsername("testuser");
    testUser.setDisplayName("Test User");
    testUser.setEmail("test@example.com");

    testRating = RatingFaker.generate(ratingId, userId, titleId, 8);

    testReview = ReviewFaker.generate(reviewId, userId, titleId, ratingId, "Great Movie Review",
        "This is a fantastic movie with excellent acting and a compelling storyline that kept me engaged from start to finish.",
        false, 5, null, LocalDateTime.now(), LocalDateTime.now());
  }

  // ========== createReview Tests ==========

  @Test
  void testCreateReview_Success() {
    // Arrange
    ReviewRequest request = new ReviewRequest();
    request.setTitleId(titleId);
    request.setTitle("Amazing Film");
    request.setContent(
        "This movie exceeded all my expectations. The cinematography was stunning and the performances were outstanding.");
    request.setRatingScore(9);
    request.setContainsSpoilers(false);

    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(reviewRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());
    when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
      Rating rating = invocation.getArgument(0);
      rating.setId(ratingId);
      return rating;
    });
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
      Review review = invocation.getArgument(0);
      review.setId(reviewId);
      review.setCreatedAt(LocalDateTime.now());
      review.setUpdatedAt(LocalDateTime.now());
      return review;
    });
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    ReviewResponse response = reviewService.createReview(userId, request);

    // Assert
    assertNotNull(response);
    assertEquals(reviewId, response.getId());
    assertEquals(userId, response.getUserId());
    assertEquals(titleId, response.getTitleId());
    assertEquals("Amazing Film", response.getReviewTitle());
    assertEquals(ratingId, response.getRatingId());

    // titleRepository.findById is called twice: once in createReview and once in
    // mapToResponse
    verify(titleRepository, atLeastOnce()).findById(titleId);
    verify(reviewRepository).findByUserIdAndTitleId(userId, titleId);
    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
    verify(ratingRepository).save(any(Rating.class));
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  void testCreateReview_TitleNotFound() {
    // Arrange
    ReviewRequest request = new ReviewRequest();
    request.setTitleId(titleId);
    request.setTitle("Test Review");
    request.setContent("This is a test review with enough content to meet the minimum requirement of 50 characters.");
    request.setRatingScore(8);

    when(titleRepository.findById(titleId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> reviewService.createReview(userId, request));

    verify(titleRepository).findById(titleId);
    verify(reviewRepository, never()).save(any(Review.class));
  }

  @Test
  void testCreateReview_AlreadyExists() {
    // Arrange
    ReviewRequest request = new ReviewRequest();
    request.setTitleId(titleId);
    request.setTitle("Test Review");
    request.setContent("This is a test review with enough content to meet the minimum requirement of 50 characters.");
    request.setRatingScore(8);

    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(reviewRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.of(testReview));

    // Act & Assert
    assertThrows(BadRequestException.class, () -> reviewService.createReview(userId, request));

    verify(titleRepository).findById(titleId);
    verify(reviewRepository).findByUserIdAndTitleId(userId, titleId);
    verify(reviewRepository, never()).save(any(Review.class));
  }

  @Test
  void testCreateReview_UpdateExistingRating() {
    // Arrange
    ReviewRequest request = new ReviewRequest();
    request.setTitleId(titleId);
    request.setTitle("Updated Review");
    request.setContent("This is a test review with enough content to meet the minimum requirement of 50 characters.");
    request.setRatingScore(10);
    request.setContainsSpoilers(false);

    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(reviewRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.of(testRating));
    when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
      Rating rating = invocation.getArgument(0);
      rating.setScore(10);
      return rating;
    });
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
      Review review = invocation.getArgument(0);
      review.setId(reviewId);
      review.setCreatedAt(LocalDateTime.now());
      review.setUpdatedAt(LocalDateTime.now());
      return review;
    });
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    ReviewResponse response = reviewService.createReview(userId, request);

    // Assert
    assertNotNull(response);
    verify(ratingRepository).save(any(Rating.class));
    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
  }

  // ========== getReviewById Tests ==========

  @Test
  void testGetReviewById_Success() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    ReviewResponse response = reviewService.getReviewById(reviewId);

    // Assert
    assertNotNull(response);
    assertEquals(reviewId, response.getId());
    assertEquals("Great Movie Review", response.getReviewTitle());

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
  }

  @Test
  void testGetReviewById_NotFound() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> reviewService.getReviewById(reviewId));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
  }

  // ========== updateReview Tests ==========

  @Test
  void testUpdateReview_Success() {
    // Arrange
    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setTitle("Updated Review Title");
    request
        .setContent("This is an updated review with enough content to meet the minimum requirement of 50 characters.");
    request.setContainsSpoilers(true);

    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    ReviewResponse response = reviewService.updateReview(userId, reviewId, request);

    // Assert
    assertNotNull(response);
    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewRepository).save(any(Review.class));
  }

  @Test
  void testUpdateReview_NotFound() {
    // Arrange
    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setTitle("Updated Title");

    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> reviewService.updateReview(userId, reviewId, request));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewRepository, never()).save(any(Review.class));
  }

  @Test
  void testUpdateReview_Forbidden() {
    // Arrange
    UUID otherUserId = UUID.randomUUID();
    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setTitle("Updated Title");

    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));

    // Act & Assert
    assertThrows(ForbiddenException.class, () -> reviewService.updateReview(otherUserId, reviewId, request));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewRepository, never()).save(any(Review.class));
  }

  @Test
  void testUpdateReview_UpdateRating() {
    // Arrange
    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setRatingScore(10);

    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.of(testRating));
    when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
      Rating rating = invocation.getArgument(0);
      rating.setScore(10);
      return rating;
    });
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    ReviewResponse response = reviewService.updateReview(userId, reviewId, request);

    // Assert
    assertNotNull(response);
    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
    verify(ratingRepository).save(any(Rating.class));
  }

  @Test
  void testUpdateReview_CreateNewRating() {
    // Arrange
    ReviewUpdateRequest request = new ReviewUpdateRequest();
    request.setRatingScore(7);

    testReview.setRatingId(null); // No existing rating

    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());
    when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
      Rating rating = invocation.getArgument(0);
      rating.setId(ratingId);
      return rating;
    });
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    ReviewResponse response = reviewService.updateReview(userId, reviewId, request);

    // Assert
    assertNotNull(response);
    verify(ratingRepository).save(any(Rating.class));
  }

  // ========== deleteReview Tests ==========

  @Test
  void testDeleteReview_Success() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    reviewService.deleteReview(userId, reviewId);

    // Assert
    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(ratingRepository).findById(ratingId);
    verify(ratingRepository).delete(testRating);
    verify(reviewRepository).save(any(Review.class));
    assertNotNull(testReview.getDeletedAt());
  }

  @Test
  void testDeleteReview_NotFound() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> reviewService.deleteReview(userId, reviewId));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(ratingRepository, never()).delete(any(Rating.class));
  }

  @Test
  void testDeleteReview_Forbidden() {
    // Arrange
    UUID otherUserId = UUID.randomUUID();
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));

    // Act & Assert
    assertThrows(ForbiddenException.class, () -> reviewService.deleteReview(otherUserId, reviewId));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(ratingRepository, never()).delete(any(Rating.class));
  }

  @Test
  void testDeleteReview_NoRating() {
    // Arrange
    testReview.setRatingId(null);
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    reviewService.deleteReview(userId, reviewId);

    // Assert
    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(ratingRepository, never()).findById(any(UUID.class));
    verify(ratingRepository, never()).delete(any(Rating.class));
    verify(reviewRepository).save(any(Review.class));
  }

  // ========== getTitleReviews Tests ==========

  @Test
  void testGetTitleReviews_Success() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    List<Review> reviews = List.of(testReview);
    Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);

    when(reviewRepository.findByTitleIdOrderByCreatedAtDesc(titleId, pageable)).thenReturn(reviewPage);
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    Page<ReviewResponse> response = reviewService.getTitleReviews(titleId, pageable);

    // Assert
    assertNotNull(response);
    assertEquals(1, response.getTotalElements());
    verify(reviewRepository).findByTitleIdOrderByCreatedAtDesc(titleId, pageable);
  }

  @Test
  void testGetTitleReviews_Empty() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<Review> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(reviewRepository.findByTitleIdOrderByCreatedAtDesc(titleId, pageable)).thenReturn(emptyPage);

    // Act
    Page<ReviewResponse> response = reviewService.getTitleReviews(titleId, pageable);

    // Assert
    assertNotNull(response);
    assertEquals(0, response.getTotalElements());
  }

  // ========== getTitleReviewsByHelpful Tests ==========

  @Test
  void testGetTitleReviewsByHelpful_Success() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    List<Review> reviews = List.of(testReview);
    Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);

    when(reviewRepository.findByTitleIdOrderByHelpfulCountDesc(titleId, pageable)).thenReturn(reviewPage);
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    Page<ReviewResponse> response = reviewService.getTitleReviewsByHelpful(titleId, pageable);

    // Assert
    assertNotNull(response);
    assertEquals(1, response.getTotalElements());
    verify(reviewRepository).findByTitleIdOrderByHelpfulCountDesc(titleId, pageable);
  }

  // ========== getUserReviews Tests ==========

  @Test
  void testGetUserReviews_Success() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    List<Review> reviews = List.of(testReview);
    Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);

    when(reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(reviewPage);
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    Page<ReviewResponse> response = reviewService.getUserReviews(userId, pageable);

    // Assert
    assertNotNull(response);
    assertEquals(1, response.getTotalElements());
    verify(reviewRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  // ========== getUserReviewForTitle Tests ==========

  @Test
  void testGetUserReviewForTitle_Success() {
    // Arrange
    when(reviewRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.of(testReview));
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
    when(titleDtoMapper.toDto(any(Title.class))).thenReturn(new TitleDto());

    // Act
    ReviewResponse response = reviewService.getUserReviewForTitle(userId, titleId);

    // Assert
    assertNotNull(response);
    assertEquals(reviewId, response.getId());
    verify(reviewRepository).findByUserIdAndTitleId(userId, titleId);
  }

  @Test
  void testGetUserReviewForTitle_NotFound() {
    // Arrange
    when(reviewRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> reviewService.getUserReviewForTitle(userId, titleId));

    verify(reviewRepository).findByUserIdAndTitleId(userId, titleId);
  }

  // ========== markHelpful Tests ==========

  @Test
  void testMarkHelpful_Success() {
    // Arrange
    UUID otherUserId = UUID.randomUUID();
    Review otherReview = ReviewFaker.generate(UUID.randomUUID(), otherUserId, titleId, null, "Other Review",
        "This is another review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);

    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(otherReview));
    when(reviewHelpfulRepository.existsByUserIdAndReviewId(userId, reviewId)).thenReturn(false);
    when(reviewHelpfulRepository.save(any(ReviewHelpful.class))).thenAnswer(invocation -> {
      ReviewHelpful helpful = invocation.getArgument(0);
      helpful.setId(UUID.randomUUID());
      return helpful;
    });

    // Act
    reviewService.markHelpful(userId, reviewId);

    // Assert
    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewHelpfulRepository).existsByUserIdAndReviewId(userId, reviewId);
    verify(reviewHelpfulRepository).save(any(ReviewHelpful.class));
  }

  @Test
  void testMarkHelpful_ReviewNotFound() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> reviewService.markHelpful(userId, reviewId));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewHelpfulRepository, never()).save(any(ReviewHelpful.class));
  }

  @Test
  void testMarkHelpful_SelfLike() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));

    // Act & Assert
    assertThrows(BadRequestException.class, () -> reviewService.markHelpful(userId, reviewId));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewHelpfulRepository, never()).save(any(ReviewHelpful.class));
  }

  @Test
  void testMarkHelpful_AlreadyMarked() {
    // Arrange
    UUID otherUserId = UUID.randomUUID();
    Review otherReview = ReviewFaker.generate(UUID.randomUUID(), otherUserId, titleId, null, "Other Review",
        "This is another review with enough content to meet the minimum requirement of 50 characters.",
        false, 0, null, null, null);

    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(otherReview));
    when(reviewHelpfulRepository.existsByUserIdAndReviewId(userId, reviewId)).thenReturn(true);

    // Act & Assert
    assertThrows(BadRequestException.class, () -> reviewService.markHelpful(userId, reviewId));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewHelpfulRepository).existsByUserIdAndReviewId(userId, reviewId);
    verify(reviewHelpfulRepository, never()).save(any(ReviewHelpful.class));
  }

  // ========== unmarkHelpful Tests ==========

  @Test
  void testUnmarkHelpful_Success() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));
    doNothing().when(reviewHelpfulRepository).deleteByUserIdAndReviewId(userId, reviewId);

    // Act
    reviewService.unmarkHelpful(userId, reviewId);

    // Assert
    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewHelpfulRepository).deleteByUserIdAndReviewId(userId, reviewId);
  }

  @Test
  void testUnmarkHelpful_ReviewNotFound() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () -> reviewService.unmarkHelpful(userId, reviewId));

    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewHelpfulRepository, never()).deleteByUserIdAndReviewId(any(), any());
  }

  @Test
  void testUnmarkHelpful_NotMarked() {
    // Arrange
    when(reviewRepository.findByIdAndNotDeleted(reviewId)).thenReturn(Optional.of(testReview));
    doNothing().when(reviewHelpfulRepository).deleteByUserIdAndReviewId(userId, reviewId);

    // Act - Should not throw exception even if not marked (implementation doesn't
    // check)
    reviewService.unmarkHelpful(userId, reviewId);

    // Assert
    verify(reviewRepository).findByIdAndNotDeleted(reviewId);
    verify(reviewHelpfulRepository).deleteByUserIdAndReviewId(userId, reviewId);
  }
}

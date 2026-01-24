package com.filmreview.service;

import com.filmreview.dto.RatingRequest;
import com.filmreview.dto.RatingResponse;
import com.filmreview.entity.Rating;
import com.filmreview.exception.BadRequestException;
import com.filmreview.exception.NotFoundException;
import com.filmreview.faker.RatingFaker;
import com.filmreview.repository.RatingRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

  @Mock
  private RatingRepository ratingRepository;

  @InjectMocks
  private RatingServiceImpl ratingService;

  private UUID userId;
  private UUID titleId;
  private Rating testRating;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    titleId = UUID.randomUUID();

    testRating = RatingFaker.generate(userId, titleId, 8);
  }

  @Test
  void testCreateOrUpdateRating_CreateNew() {
    RatingRequest request = new RatingRequest();
    request.setScore(8);

    // Mock: no existing rating
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());
    when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
      Rating rating = invocation.getArgument(0);
      if (rating.getId() == null) {
        rating.setId(UUID.randomUUID());
      }
      if (rating.getCreatedAt() == null) {
        rating.setCreatedAt(LocalDateTime.now());
      }
      rating.setUpdatedAt(LocalDateTime.now());
      return rating;
    });

    RatingResponse response = ratingService.createOrUpdateRating(userId, titleId, request);

    assertNotNull(response.getId());
    assertEquals(8, response.getScore());
    assertEquals(userId, response.getUserId());
    assertEquals(titleId, response.getTitleId());
    assertNotNull(response.getCreatedAt());

    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
    verify(ratingRepository).save(any(Rating.class));
  }

  @Test
  void testCreateOrUpdateRating_UpdateExisting() {
    // Mock: existing rating found
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.of(testRating));
    when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
      Rating rating = invocation.getArgument(0);
      rating.setUpdatedAt(LocalDateTime.now());
      return rating;
    });

    RatingRequest request = new RatingRequest();
    request.setScore(9);

    RatingResponse response = ratingService.createOrUpdateRating(userId, titleId, request);

    // Should be same rating ID
    assertEquals(testRating.getId(), response.getId());
    assertEquals(9, response.getScore());
    assertNotNull(response.getUpdatedAt());

    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
    verify(ratingRepository).save(any(Rating.class));
  }

  @Test
  void testCreateOrUpdateRating_InvalidScore_TooLow() {
    RatingRequest request = new RatingRequest();
    request.setScore(0);

    assertThrows(BadRequestException.class,
        () -> ratingService.createOrUpdateRating(userId, titleId, request));

    verify(ratingRepository, never()).save(any(Rating.class));
  }

  @Test
  void testCreateOrUpdateRating_InvalidScore_TooHigh() {
    RatingRequest request = new RatingRequest();
    request.setScore(11);

    assertThrows(BadRequestException.class,
        () -> ratingService.createOrUpdateRating(userId, titleId, request));

    verify(ratingRepository, never()).save(any(Rating.class));
  }

  @Test
  void testCreateOrUpdateRating_InvalidScore_Null() {
    RatingRequest request = new RatingRequest();
    request.setScore(null);

    assertThrows(BadRequestException.class,
        () -> ratingService.createOrUpdateRating(userId, titleId, request));

    verify(ratingRepository, never()).save(any(Rating.class));
  }

  @Test
  void testDeleteRating_Success() {
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.of(testRating));
    doNothing().when(ratingRepository).delete(any(Rating.class));

    assertDoesNotThrow(() -> ratingService.deleteRating(userId, titleId));

    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
    verify(ratingRepository).delete(testRating);
  }

  @Test
  void testDeleteRating_NotFound() {
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> ratingService.deleteRating(userId, titleId));

    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
    verify(ratingRepository, never()).delete(any(Rating.class));
  }

  @Test
  void testGetUserRatings() {
    UUID titleId2 = UUID.randomUUID();
    UUID titleId3 = UUID.randomUUID();

    Rating rating1 = RatingFaker.generate(userId, titleId, 8);
    Rating rating2 = RatingFaker.generate(userId, titleId2, 9);
    Rating rating3 = RatingFaker.generate(userId, titleId3, 10);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Rating> ratingPage = new PageImpl<>(List.of(rating1, rating2, rating3), pageable, 3);

    when(ratingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(ratingPage);

    Page<RatingResponse> ratings = ratingService.getUserRatings(userId, pageable);

    assertEquals(3, ratings.getTotalElements());
    assertEquals(3, ratings.getContent().size());

    verify(ratingRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Test
  void testGetUserRatings_Pagination() {
    Rating rating1 = RatingFaker.generate(userId, UUID.randomUUID(), 8);
    Rating rating2 = RatingFaker.generate(userId, UUID.randomUUID(), 8);

    Pageable pageable = PageRequest.of(0, 2);
    Page<Rating> ratingPage = new PageImpl<>(List.of(rating1, rating2), pageable, 5);

    when(ratingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(ratingPage);

    Page<RatingResponse> ratings = ratingService.getUserRatings(userId, pageable);

    assertEquals(5, ratings.getTotalElements());
    assertEquals(2, ratings.getContent().size());
    assertTrue(ratings.hasNext());

    verify(ratingRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Test
  void testGetRating_Success() {
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.of(testRating));

    RatingResponse found = ratingService.getRating(userId, titleId);

    assertNotNull(found);
    assertEquals(testRating.getId(), found.getId());
    assertEquals(8, found.getScore());

    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
  }

  @Test
  void testGetRating_NotFound() {
    when(ratingRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> ratingService.getRating(userId, titleId));

    verify(ratingRepository).findByUserIdAndTitleId(userId, titleId);
  }

  @Test
  void testGetTitleRatings() {
    UUID userId2 = UUID.randomUUID();

    Rating rating1 = RatingFaker.generate(userId, titleId, 8);
    Rating rating2 = RatingFaker.generate(userId2, titleId, 9);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Rating> ratingPage = new PageImpl<>(List.of(rating1, rating2), pageable, 2);

    when(ratingRepository.findByTitleIdOrderByCreatedAtDesc(titleId, pageable)).thenReturn(ratingPage);

    Page<RatingResponse> ratings = ratingService.getTitleRatings(titleId, pageable);

    assertEquals(2, ratings.getTotalElements());
    assertEquals(2, ratings.getContent().size());

    verify(ratingRepository).findByTitleIdOrderByCreatedAtDesc(titleId, pageable);
  }

  @Test
  void testGetTitleRatings_Empty() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Rating> emptyPage = new PageImpl<>(List.of(), pageable, 0);

    when(ratingRepository.findByTitleIdOrderByCreatedAtDesc(titleId, pageable)).thenReturn(emptyPage);

    Page<RatingResponse> ratings = ratingService.getTitleRatings(titleId, pageable);

    assertEquals(0, ratings.getTotalElements());
    assertTrue(ratings.getContent().isEmpty());

    verify(ratingRepository).findByTitleIdOrderByCreatedAtDesc(titleId, pageable);
  }
}

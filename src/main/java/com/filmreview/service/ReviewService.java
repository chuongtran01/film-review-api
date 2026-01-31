package com.filmreview.service;

import com.filmreview.dto.ReviewRequest;
import com.filmreview.dto.ReviewResponse;
import com.filmreview.dto.ReviewUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Interface for managing reviews.
 */
public interface ReviewService {

  /**
   * Create a review for a title.
   */
  ReviewResponse createReview(UUID userId, ReviewRequest request);

  /**
   * Get a review by ID.
   */
  ReviewResponse getReviewById(UUID reviewId);

  /**
   * Update a review.
   */
  ReviewResponse updateReview(UUID userId, UUID reviewId, ReviewUpdateRequest request);

  /**
   * Delete a review (soft delete).
   */
  void deleteReview(UUID userId, UUID reviewId);

  /**
   * Get all reviews for a specific title.
   */
  Page<ReviewResponse> getTitleReviews(UUID titleId, Pageable pageable);

  /**
   * Get all reviews for a specific title, sorted by helpful count.
   */
  Page<ReviewResponse> getTitleReviewsByHelpful(UUID titleId, Pageable pageable);

  /**
   * Get all reviews for a specific user.
   */
  Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable);

  /**
   * Get a user's review for a specific title.
   */
  ReviewResponse getUserReviewForTitle(UUID userId, UUID titleId);

  /**
   * Mark a review as helpful.
   */
  void markHelpful(UUID userId, UUID reviewId);

  /**
   * Unmark a review as helpful.
   */
  void unmarkHelpful(UUID userId, UUID reviewId);
}

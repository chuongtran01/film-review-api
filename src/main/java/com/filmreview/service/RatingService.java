package com.filmreview.service;

import com.filmreview.dto.RatingRequest;
import com.filmreview.dto.RatingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Interface for managing ratings.
 */
public interface RatingService {

  /**
   * Create or update a rating for a title.
   * If the user has already rated the title, update the existing rating.
   * Otherwise, create a new rating.
   */
  RatingResponse createOrUpdateRating(UUID userId, UUID titleId, RatingRequest request);

  /**
   * Delete a rating for a title.
   */
  void deleteRating(UUID userId, UUID titleId);

  /**
   * Get all ratings for the current user.
   */
  Page<RatingResponse> getUserRatings(UUID userId, Pageable pageable);

  /**
   * Get a specific rating by user and title.
   */
  RatingResponse getRating(UUID userId, UUID titleId);

  /**
   * Get all ratings for a specific title.
   */
  Page<RatingResponse> getTitleRatings(UUID titleId, Pageable pageable);
}

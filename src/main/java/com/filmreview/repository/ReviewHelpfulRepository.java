package com.filmreview.repository;

import com.filmreview.entity.ReviewHelpful;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, UUID> {

  /**
   * Check if a user has marked a review as helpful.
   */
  boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);

  /**
   * Find a helpful vote by user and review.
   */
  Optional<ReviewHelpful> findByUserIdAndReviewId(UUID userId, UUID reviewId);

  /**
   * Delete a helpful vote by user and review.
   */
  void deleteByUserIdAndReviewId(UUID userId, UUID reviewId);
}

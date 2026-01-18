package com.filmreview.repository;

import com.filmreview.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

  /**
   * Find all ratings for a specific user.
   */
  Page<Rating> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

  /**
   * Find a specific rating by user and title.
   */
  Optional<Rating> findByUserIdAndTitleId(UUID userId, UUID titleId);

  /**
   * Find all ratings for a specific title.
   */
  Page<Rating> findByTitleIdOrderByCreatedAtDesc(UUID titleId, Pageable pageable);

  /**
   * Check if a user has rated a title.
   */
  boolean existsByUserIdAndTitleId(UUID userId, UUID titleId);

  /**
   * Count ratings for a specific title.
   */
  long countByTitleId(UUID titleId);

  /**
   * Count ratings for a specific user.
   */
  long countByUserId(UUID userId);

  /**
   * Get average rating for a specific title.
   */
  @Query("SELECT AVG(r.score) FROM Rating r WHERE r.titleId = :titleId")
  Double getAverageRatingByTitleId(@Param("titleId") UUID titleId);
}

package com.filmreview.repository;

import com.filmreview.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  /**
   * Count reviews for a specific user.
   */
  long countByUserId(UUID userId);

  /**
   * Find all reviews for a specific user.
   */
  Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

  /**
   * Find all reviews for a specific title.
   */
  Page<Review> findByTitleIdOrderByCreatedAtDesc(UUID titleId, Pageable pageable);

  /**
   * Find a specific review by user and title.
   */
  Optional<Review> findByUserIdAndTitleId(UUID userId, UUID titleId);
}

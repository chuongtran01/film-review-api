package com.filmreview.repository;

import com.filmreview.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  /**
   * Count reviews for a specific user (excluding soft-deleted).
   */
  @Query("SELECT COUNT(r) FROM Review r WHERE r.userId = :userId AND r.deletedAt IS NULL")
  long countByUserId(@Param("userId") UUID userId);

  /**
   * Find all reviews for a specific user (excluding soft-deleted).
   */
  @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
  Page<Review> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

  /**
   * Find all reviews for a specific title (excluding soft-deleted).
   */
  @Query("SELECT r FROM Review r WHERE r.titleId = :titleId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
  Page<Review> findByTitleIdOrderByCreatedAtDesc(@Param("titleId") UUID titleId, Pageable pageable);

  /**
   * Find all reviews for a specific title ordered by helpful count (excluding
   * soft-deleted).
   */
  @Query("SELECT r FROM Review r WHERE r.titleId = :titleId AND r.deletedAt IS NULL ORDER BY r.helpfulCount DESC, r.createdAt DESC")
  Page<Review> findByTitleIdOrderByHelpfulCountDesc(@Param("titleId") UUID titleId, Pageable pageable);

  /**
   * Find a specific review by user and title (excluding soft-deleted).
   */
  @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.titleId = :titleId AND r.deletedAt IS NULL")
  Optional<Review> findByUserIdAndTitleId(@Param("userId") UUID userId, @Param("titleId") UUID titleId);

  /**
   * Find a review by ID (excluding soft-deleted).
   */
  @Query("SELECT r FROM Review r WHERE r.id = :id AND r.deletedAt IS NULL")
  Optional<Review> findByIdAndNotDeleted(@Param("id") UUID id);
}

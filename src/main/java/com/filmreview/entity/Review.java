package com.filmreview.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Review entity representing a user's written review for a title.
 * Reviews can optionally link to a rating.
 */
@Entity
@Table(name = "reviews")
// Note: Unique constraint is enforced via partial unique index
// (idx_reviews_user_title_unique)
// which excludes soft-deleted records (WHERE deleted_at IS NULL)
@Getter
@Setter
@NoArgsConstructor
public class Review extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "title_id", nullable = false)
  private UUID titleId;

  @Column(name = "rating_id")
  private UUID ratingId; // Optional link to rating

  @Size(min = 1, max = 200)
  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "content", columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(name = "contains_spoilers", nullable = false)
  private Boolean containsSpoilers = false;

  @Column(name = "helpful_count", nullable = false)
  private Integer helpfulCount = 0;

  @Column(name = "deleted_at")
  private java.time.LocalDateTime deletedAt; // Soft delete
}

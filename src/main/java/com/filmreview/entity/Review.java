package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Review entity representing a user's written review for a title.
 * Reviews can optionally link to a rating.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "title_id" })
})
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

  @Column(name = "content", columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(name = "contains_spoilers", nullable = false)
  private Boolean containsSpoilers = false;

  @Column(name = "helpful_count", nullable = false)
  private Integer helpfulCount = 0;

  @Column(name = "deleted_at")
  private java.time.LocalDateTime deletedAt; // Soft delete
}

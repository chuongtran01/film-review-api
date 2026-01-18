package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Rating entity representing a user's rating (1-10) for a title.
 * Each user can only have one rating per title (enforced by unique constraint).
 */
@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "title_id" })
}, indexes = {
    @Index(name = "idx_ratings_user_id", columnList = "user_id"),
    @Index(name = "idx_ratings_title_id", columnList = "title_id"),
    @Index(name = "idx_ratings_user_title", columnList = "user_id, title_id"),
    @Index(name = "idx_ratings_title_score", columnList = "title_id, score"),
    @Index(name = "idx_ratings_title_created", columnList = "title_id, created_at DESC"),
    @Index(name = "idx_ratings_user_created", columnList = "user_id, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class Rating extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "title_id", nullable = false)
  private UUID titleId;

  @Column(name = "score", nullable = false)
  private Integer score; // 1-10

  // Note: We don't add @ManyToOne relationships to User/Title yet
  // because those entities might not exist or might cause circular dependencies.
  // We'll add them later if needed for queries.
}

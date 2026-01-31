package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * ReviewHelpful entity representing a user's helpful vote on a review.
 */
@Entity
@Table(name = "review_helpful", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "review_id" })
})
@Getter
@Setter
@NoArgsConstructor
public class ReviewHelpful extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;
}

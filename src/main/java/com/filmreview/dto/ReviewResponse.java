package com.filmreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

  private UUID id;

  private UUID userId;

  private UUID titleId;

  private UUID ratingId; // Optional link to rating

  private String reviewTitle; // Review title/headline

  private String content;

  private Boolean containsSpoilers;

  private Integer helpfulCount;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  // Nested objects
  private UserResponse user; // User who wrote the review

  private TitleDto title; // Movie/TV show being reviewed

  private RatingResponse rating; // Optional linked rating
}

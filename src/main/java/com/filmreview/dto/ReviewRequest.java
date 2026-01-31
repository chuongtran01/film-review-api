package com.filmreview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

  @NotNull(message = "Title ID is required")
  private UUID titleId;

  @NotBlank(message = "Review title is required")
  @Size(min = 1, max = 200, message = "Review title must be between 1 and 200 characters")
  private String title;

  @NotBlank(message = "Content is required")
  @Size(min = 50, max = 5000, message = "Content must be between 50 and 5000 characters")
  private String content;

  @NotNull(message = "Rating is required")
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 10, message = "Rating must be at most 10")
  private Integer ratingScore; // Required rating score (1-10)

  private Boolean containsSpoilers = false;
}

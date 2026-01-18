package com.filmreview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

  @NotNull(message = "Score is required")
  @Min(value = 1, message = "Score must be at least 1")
  @Max(value = 10, message = "Score must be at most 10")
  private Integer score;
}

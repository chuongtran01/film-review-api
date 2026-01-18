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
public class RatingResponse {

  private UUID id;
  private UUID userId;
  private UUID titleId;
  private Integer score;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

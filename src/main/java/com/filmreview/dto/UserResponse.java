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
public class UserResponse {
  private UUID id;
  private String username;
  private String email;
  private String displayName;
  private String avatarUrl;
  private String bio;
  private Boolean verified;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private UserStats stats;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserStats {
    private Long reviewsCount;
    private Long ratingsCount;
    private Long watchlistCount;
  }
}

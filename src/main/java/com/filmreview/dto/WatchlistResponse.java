package com.filmreview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.filmreview.entity.Watchlist;
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
public class WatchlistResponse {

  private UUID id;

  @JsonProperty("user_id")
  private UUID userId;

  @JsonProperty("title_id")
  private UUID titleId;

  private Watchlist.WatchlistStatus status;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  // Optional: Include full title information for watchlist listing
  private TitleDto title;
}

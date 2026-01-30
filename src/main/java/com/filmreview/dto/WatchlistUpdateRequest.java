package com.filmreview.dto;

import com.filmreview.entity.Watchlist;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistUpdateRequest {

  @NotNull(message = "Status is required")
  private Watchlist.WatchlistStatus status;
}

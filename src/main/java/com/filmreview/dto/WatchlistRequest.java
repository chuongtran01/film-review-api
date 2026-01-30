package com.filmreview.dto;

import com.filmreview.entity.Watchlist;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistRequest {

  @NotNull(message = "Title ID is required")
  private UUID titleId;

  private Watchlist.WatchlistStatus status = Watchlist.WatchlistStatus.WANT_TO_WATCH;
}

package com.filmreview.service;

import com.filmreview.dto.WatchlistRequest;
import com.filmreview.dto.WatchlistResponse;
import com.filmreview.dto.WatchlistUpdateRequest;
import com.filmreview.entity.Watchlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing watchlist operations.
 */
public interface WatchlistService {

  /**
   * Get user's watchlist with optional status filter.
   */
  Page<WatchlistResponse> getUserWatchlist(UUID userId, Watchlist.WatchlistStatus status, Pageable pageable);

  /**
   * Add a title to watchlist or update if already exists.
   */
  WatchlistResponse addToWatchlist(UUID userId, WatchlistRequest request);

  /**
   * Update watchlist status for a specific title.
   */
  WatchlistResponse updateWatchlistStatus(UUID userId, UUID titleId, WatchlistUpdateRequest request);

  /**
   * Remove a title from watchlist.
   */
  void removeFromWatchlist(UUID userId, UUID titleId);

  /**
   * Get a specific watchlist item by user and title.
   */
  Optional<WatchlistResponse> getWatchlistItem(UUID userId, UUID titleId);
}

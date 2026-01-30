package com.filmreview.controller;

import com.filmreview.dto.WatchlistRequest;
import com.filmreview.dto.WatchlistResponse;
import com.filmreview.dto.WatchlistUpdateRequest;
import com.filmreview.entity.Watchlist;
import com.filmreview.security.UserPrincipal;
import com.filmreview.service.WatchlistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/watchlist")
@PreAuthorize("isAuthenticated()")
public class WatchlistController {

  private final WatchlistService watchlistService;

  public WatchlistController(WatchlistService watchlistService) {
    this.watchlistService = watchlistService;
  }

  /**
   * Get user's watchlist with optional status filter.
   * GET /api/v1/watchlist?status={status}&page={page}&size={size}
   */
  @GetMapping
  public ResponseEntity<Page<WatchlistResponse>> getUserWatchlist(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @RequestParam(required = false) Watchlist.WatchlistStatus status,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<WatchlistResponse> watchlist = watchlistService.getUserWatchlist(
        currentUser.getId(),
        status,
        pageable);
    return ResponseEntity.ok(watchlist);
  }

  /**
   * Add a title to watchlist.
   * POST /api/v1/watchlist
   */
  @PostMapping
  public ResponseEntity<WatchlistResponse> addToWatchlist(
      @Valid @RequestBody WatchlistRequest request,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    WatchlistResponse response = watchlistService.addToWatchlist(currentUser.getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update watchlist status for a specific title.
   * PATCH /api/v1/watchlist/{titleId}
   */
  @PatchMapping("/{titleId}")
  public ResponseEntity<WatchlistResponse> updateWatchlistStatus(
      @PathVariable UUID titleId,
      @Valid @RequestBody WatchlistUpdateRequest request,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    WatchlistResponse response = watchlistService.updateWatchlistStatus(
        currentUser.getId(),
        titleId,
        request);
    return ResponseEntity.ok(response);
  }

  /**
   * Remove a title from watchlist.
   * DELETE /api/v1/watchlist/{titleId}
   */
  @DeleteMapping("/{titleId}")
  public ResponseEntity<Void> removeFromWatchlist(
      @PathVariable UUID titleId,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    watchlistService.removeFromWatchlist(currentUser.getId(), titleId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get a specific watchlist item by title.
   * GET /api/v1/watchlist/{titleId}
   */
  @GetMapping("/{titleId}")
  public ResponseEntity<WatchlistResponse> getWatchlistItem(
      @PathVariable UUID titleId,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    Optional<WatchlistResponse> watchlistItem = watchlistService.getWatchlistItem(
        currentUser.getId(),
        titleId);

    if (watchlistItem.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(watchlistItem.get());
  }
}

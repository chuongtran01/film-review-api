package com.filmreview.controller;

import com.filmreview.dto.RatingRequest;
import com.filmreview.dto.RatingResponse;
import com.filmreview.security.UserPrincipal;
import com.filmreview.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ratings")
@PreAuthorize("isAuthenticated()")
public class RatingController {

  private final RatingService ratingService;

  public RatingController(RatingService ratingService) {
    this.ratingService = ratingService;
  }

  /**
   * Create or update a rating for a title.
   * PUT /api/v1/ratings/{titleId}
   */
  @PutMapping("/{titleId}")
  public ResponseEntity<RatingResponse> createOrUpdateRating(
      @PathVariable UUID titleId,
      @Valid @RequestBody RatingRequest request,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    RatingResponse response = ratingService.createOrUpdateRating(
        currentUser.getId(),
        titleId,
        request);
    return ResponseEntity.ok(response);
  }

  /**
   * Delete a rating for a title.
   * DELETE /api/v1/ratings/{titleId}
   */
  @DeleteMapping("/{titleId}")
  public ResponseEntity<Void> deleteRating(
      @PathVariable UUID titleId,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    ratingService.deleteRating(currentUser.getId(), titleId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get the current user's rating for a specific title.
   * GET /api/v1/ratings/{titleId}
   */
  @GetMapping("/{titleId}")
  public ResponseEntity<RatingResponse> getRating(
      @PathVariable UUID titleId,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    RatingResponse response = ratingService.getRating(currentUser.getId(), titleId);
    return ResponseEntity.ok(response);
  }

  /**
   * Get all ratings for the current user.
   * GET /api/v1/ratings
   */
  @GetMapping
  public ResponseEntity<Page<RatingResponse>> getUserRatings(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<RatingResponse> ratings = ratingService.getUserRatings(currentUser.getId(), pageable);
    return ResponseEntity.ok(ratings);
  }

  /**
   * Get all ratings for a specific title (public endpoint).
   * GET /api/v1/ratings/titles/{titleId}
   */
  @GetMapping("/titles/{titleId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Page<RatingResponse>> getTitleRatings(
      @PathVariable UUID titleId,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<RatingResponse> ratings = ratingService.getTitleRatings(titleId, pageable);
    return ResponseEntity.ok(ratings);
  }
}

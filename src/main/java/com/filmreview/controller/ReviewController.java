package com.filmreview.controller;

import com.filmreview.dto.ReviewRequest;
import com.filmreview.dto.ReviewResponse;
import com.filmreview.dto.ReviewUpdateRequest;
import com.filmreview.security.UserPrincipal;
import com.filmreview.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  /**
   * Create a review for a title.
   * POST /api/v1/reviews
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ReviewResponse> createReview(
      @Valid @RequestBody ReviewRequest request,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    ReviewResponse response = reviewService.createReview(currentUser.getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Get a review by ID.
   * GET /api/v1/reviews/{id}
   */
  @GetMapping("/{id}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<ReviewResponse> getReviewById(@PathVariable UUID id) {
    ReviewResponse response = reviewService.getReviewById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Update a review.
   * PATCH /api/v1/reviews/{id}
   */
  @PatchMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ReviewResponse> updateReview(
      @PathVariable UUID id,
      @Valid @RequestBody ReviewUpdateRequest request,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    ReviewResponse response = reviewService.updateReview(currentUser.getId(), id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Delete a review (soft delete).
   * DELETE /api/v1/reviews/{id}
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> deleteReview(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    reviewService.deleteReview(currentUser.getId(), id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get all reviews for the current user.
   * GET /api/v1/reviews
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Page<ReviewResponse>> getUserReviews(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<ReviewResponse> reviews = reviewService.getUserReviews(currentUser.getId(), pageable);
    return ResponseEntity.ok(reviews);
  }

  /**
   * Get all reviews for a specific title (public endpoint).
   * GET /api/v1/reviews/titles/{titleId}
   */
  @GetMapping("/titles/{titleId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<Page<ReviewResponse>> getTitleReviews(
      @PathVariable UUID titleId,
      @RequestParam(required = false, defaultValue = "newest") String sort,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<ReviewResponse> reviews;
    if ("helpful".equals(sort)) {
      reviews = reviewService.getTitleReviewsByHelpful(titleId, pageable);
    } else {
      reviews = reviewService.getTitleReviews(titleId, pageable);
    }
    return ResponseEntity.ok(reviews);
  }

  /**
   * Get the current user's review for a specific title.
   * GET /api/v1/reviews/titles/{titleId}/me
   */
  @GetMapping("/titles/{titleId}/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ReviewResponse> getUserReviewForTitle(
      @PathVariable UUID titleId,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    try {
      ReviewResponse response = reviewService.getUserReviewForTitle(currentUser.getId(), titleId);
      return ResponseEntity.ok(response);
    } catch (com.filmreview.exception.NotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Mark a review as helpful.
   * POST /api/v1/reviews/{id}/helpful
   */
  @PostMapping("/{id}/helpful")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> markHelpful(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    reviewService.markHelpful(currentUser.getId(), id);
    return ResponseEntity.ok().build();
  }

  /**
   * Unmark a review as helpful.
   * DELETE /api/v1/reviews/{id}/helpful
   */
  @DeleteMapping("/{id}/helpful")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> unmarkHelpful(
      @PathVariable UUID id,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    reviewService.unmarkHelpful(currentUser.getId(), id);
    return ResponseEntity.ok().build();
  }
}

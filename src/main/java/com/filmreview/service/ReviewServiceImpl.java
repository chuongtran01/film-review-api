package com.filmreview.service;

import com.filmreview.dto.ReviewRequest;
import com.filmreview.dto.ReviewResponse;
import com.filmreview.dto.ReviewUpdateRequest;
import com.filmreview.dto.RatingResponse;
import com.filmreview.dto.UserResponse;
import com.filmreview.entity.Review;
import com.filmreview.entity.ReviewHelpful;
import com.filmreview.entity.Rating;
import com.filmreview.entity.Title;
import com.filmreview.entity.User;
import com.filmreview.exception.BadRequestException;
import com.filmreview.exception.ForbiddenException;
import com.filmreview.exception.NotFoundException;
import com.filmreview.mapper.TitleDtoMapper;
import com.filmreview.repository.ReviewRepository;
import com.filmreview.repository.ReviewHelpfulRepository;
import com.filmreview.repository.RatingRepository;
import com.filmreview.repository.TitleRepository;
import com.filmreview.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewHelpfulRepository reviewHelpfulRepository;
  private final RatingRepository ratingRepository;
  private final TitleRepository titleRepository;
  private final UserRepository userRepository;
  private final TitleDtoMapper titleDtoMapper;

  public ReviewServiceImpl(
      ReviewRepository reviewRepository,
      ReviewHelpfulRepository reviewHelpfulRepository,
      RatingRepository ratingRepository,
      TitleRepository titleRepository,
      UserRepository userRepository,
      TitleDtoMapper titleDtoMapper) {
    this.reviewRepository = reviewRepository;
    this.reviewHelpfulRepository = reviewHelpfulRepository;
    this.ratingRepository = ratingRepository;
    this.titleRepository = titleRepository;
    this.userRepository = userRepository;
    this.titleDtoMapper = titleDtoMapper;
  }

  @Override
  @Transactional
  public ReviewResponse createReview(UUID userId, ReviewRequest request) {
    // Validate title exists
    titleRepository.findById(request.getTitleId())
        .orElseThrow(() -> new NotFoundException("Title not found"));

    // Check if review already exists
    if (reviewRepository.findByUserIdAndTitleId(userId, request.getTitleId()).isPresent()) {
      throw new BadRequestException("Review already exists for this title");
    }

    // Create or update rating first (required)
    Rating rating = ratingRepository.findByUserIdAndTitleId(userId, request.getTitleId())
        .orElse(new Rating());
    rating.setUserId(userId);
    rating.setTitleId(request.getTitleId());
    rating.setScore(request.getRatingScore());
    rating = ratingRepository.save(rating);

    // Create review and link to rating
    Review review = new Review();
    review.setUserId(userId);
    review.setTitleId(request.getTitleId());
    review.setRatingId(rating.getId());
    review.setTitle(request.getTitle());
    review.setContent(request.getContent());
    review.setContainsSpoilers(request.getContainsSpoilers() != null ? request.getContainsSpoilers() : false);
    review.setHelpfulCount(0);

    review = reviewRepository.save(review);

    return mapToResponse(review);
  }

  @Override
  public ReviewResponse getReviewById(UUID reviewId) {
    Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found"));
    return mapToResponse(review);
  }

  @Override
  @Transactional
  public ReviewResponse updateReview(UUID userId, UUID reviewId, ReviewUpdateRequest request) {
    Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found"));

    // Verify ownership
    if (!review.getUserId().equals(userId)) {
      throw new ForbiddenException("Not authorized to update this review");
    }

    // Update fields
    if (request.getTitle() != null) {
      review.setTitle(request.getTitle());
    }
    if (request.getContent() != null) {
      review.setContent(request.getContent());
    }
    if (request.getContainsSpoilers() != null) {
      review.setContainsSpoilers(request.getContainsSpoilers());
    }

    // Update rating if provided
    if (request.getRatingScore() != null) {
      Rating rating = ratingRepository.findByUserIdAndTitleId(userId, review.getTitleId())
          .orElse(new Rating());
      rating.setUserId(userId);
      rating.setTitleId(review.getTitleId());
      rating.setScore(request.getRatingScore());
      rating = ratingRepository.save(rating);
      review.setRatingId(rating.getId());
    }

    review = reviewRepository.save(review);

    return mapToResponse(review);
  }

  @Override
  @Transactional
  public void deleteReview(UUID userId, UUID reviewId) {
    Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found"));

    // Verify ownership
    if (!review.getUserId().equals(userId)) {
      throw new ForbiddenException("Not authorized to delete this review");
    }

    // Delete the associated rating if it exists
    if (review.getRatingId() != null) {
      ratingRepository.findById(review.getRatingId())
          .ifPresent(rating -> ratingRepository.delete(rating));
    }

    // Soft delete the review
    review.setDeletedAt(LocalDateTime.now());
    reviewRepository.save(review);
  }

  @Override
  public Page<ReviewResponse> getTitleReviews(UUID titleId, Pageable pageable) {
    // Create Pageable without sort since our custom query already has ORDER BY
    Pageable pageableWithoutSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    Page<Review> reviews = reviewRepository.findByTitleIdOrderByCreatedAtDesc(titleId, pageableWithoutSort);
    return reviews.map(this::mapToResponse);
  }

  @Override
  public Page<ReviewResponse> getTitleReviewsByHelpful(UUID titleId, Pageable pageable) {
    // Create Pageable without sort since our custom query already has ORDER BY
    Pageable pageableWithoutSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    Page<Review> reviews = reviewRepository.findByTitleIdOrderByHelpfulCountDesc(titleId, pageableWithoutSort);
    return reviews.map(this::mapToResponse);
  }

  @Override
  public Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable) {
    // Create Pageable without sort since our custom query already has ORDER BY
    Pageable pageableWithoutSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    Page<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageableWithoutSort);
    return reviews.map(this::mapToResponse);
  }

  @Override
  public ReviewResponse getUserReviewForTitle(UUID userId, UUID titleId) {
    Review review = reviewRepository.findByUserIdAndTitleId(userId, titleId)
        .orElseThrow(() -> new NotFoundException("Review not found"));
    return mapToResponse(review);
  }

  @Override
  @Transactional
  public void markHelpful(UUID userId, UUID reviewId) {
    // Verify review exists and is not deleted
    Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
        .orElseThrow(() -> new NotFoundException("Review not found"));

    // Prevent self-liking
    if (review.getUserId().equals(userId)) {
      throw new BadRequestException("Cannot mark your own review as helpful");
    }

    // Check if already marked
    if (reviewHelpfulRepository.existsByUserIdAndReviewId(userId, reviewId)) {
      throw new BadRequestException("Review already marked as helpful");
    }

    // Create helpful vote
    ReviewHelpful reviewHelpful = new ReviewHelpful();
    reviewHelpful.setUserId(userId);
    reviewHelpful.setReviewId(reviewId);
    reviewHelpfulRepository.save(reviewHelpful);

    // Note: helpful_count is updated automatically by database trigger
  }

  @Override
  @Transactional
  public void unmarkHelpful(UUID userId, UUID reviewId) {
    // Verify review exists
    if (!reviewRepository.findByIdAndNotDeleted(reviewId).isPresent()) {
      throw new NotFoundException("Review not found");
    }

    // Remove helpful vote
    reviewHelpfulRepository.deleteByUserIdAndReviewId(userId, reviewId);

    // Note: helpful_count is updated automatically by database trigger
  }

  private ReviewResponse mapToResponse(Review review) {
    ReviewResponse response = new ReviewResponse();
    response.setId(review.getId());
    response.setUserId(review.getUserId());
    response.setTitleId(review.getTitleId());
    response.setRatingId(review.getRatingId());
    response.setReviewTitle(review.getTitle());
    response.setContent(review.getContent());
    response.setContainsSpoilers(review.getContainsSpoilers());
    response.setHelpfulCount(review.getHelpfulCount());
    response.setCreatedAt(review.getCreatedAt());
    response.setUpdatedAt(review.getUpdatedAt());

    // Fetch and map user
    User user = userRepository.findById(review.getUserId())
        .orElse(null);
    if (user != null) {
      response.setUser(mapToUserResponse(user, false)); // false = don't include email (public)
    }

    // Fetch and map title (movie/tv show)
    Title title = titleRepository.findById(review.getTitleId())
        .orElse(null);
    if (title != null) {
      response.setTitle(titleDtoMapper.toDto(title));
    }

    // Fetch and map rating if linked
    if (review.getRatingId() != null) {
      Rating rating = ratingRepository.findById(review.getRatingId())
          .orElse(null);
      if (rating != null) {
        response.setRating(mapToRatingResponse(rating));
      }
    }

    return response;
  }

  private UserResponse mapToUserResponse(User user, boolean includeEmail) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    if (includeEmail) {
      response.setEmail(user.getEmail());
    }
    response.setDisplayName(user.getDisplayName());
    response.setAvatarUrl(user.getAvatarUrl());
    response.setBio(user.getBio());
    response.setVerified(user.getVerified());
    response.setCreatedAt(user.getCreatedAt());
    response.setUpdatedAt(user.getUpdatedAt());
    // Don't include stats for review responses (not needed)
    return response;
  }

  private RatingResponse mapToRatingResponse(Rating rating) {
    RatingResponse response = new RatingResponse();
    response.setId(rating.getId());
    response.setUserId(rating.getUserId());
    response.setTitleId(rating.getTitleId());
    response.setScore(rating.getScore());
    response.setCreatedAt(rating.getCreatedAt());
    response.setUpdatedAt(rating.getUpdatedAt());
    return response;
  }
}

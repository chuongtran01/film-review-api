package com.filmreview.service;

import com.filmreview.dto.RatingRequest;
import com.filmreview.dto.RatingResponse;
import com.filmreview.entity.Rating;
import com.filmreview.exception.BadRequestException;
import com.filmreview.exception.NotFoundException;
import com.filmreview.repository.RatingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RatingServiceImpl implements RatingService {

  private final RatingRepository ratingRepository;

  public RatingServiceImpl(RatingRepository ratingRepository) {
    this.ratingRepository = ratingRepository;
  }

  @Override
  @Transactional
  public RatingResponse createOrUpdateRating(UUID userId, UUID titleId, RatingRequest request) {
    // Validate score
    if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 10) {
      throw new BadRequestException("Score must be between 1 and 10");
    }

    // Check if rating already exists
    Rating rating = ratingRepository.findByUserIdAndTitleId(userId, titleId)
        .orElse(new Rating());

    // Set or update fields
    rating.setUserId(userId);
    rating.setTitleId(titleId);
    rating.setScore(request.getScore());

    // Save (insert or update)
    rating = ratingRepository.save(rating);

    // Note: Title aggregates are updated automatically by database trigger
    // (update_title_rating_aggregates function)

    return mapToResponse(rating);
  }

  @Override
  @Transactional
  public void deleteRating(UUID userId, UUID titleId) {
    Rating rating = ratingRepository.findByUserIdAndTitleId(userId, titleId)
        .orElseThrow(() -> new NotFoundException("Rating not found"));

    ratingRepository.delete(rating);

    // Note: Title aggregates are updated automatically by database trigger
  }

  @Override
  public Page<RatingResponse> getUserRatings(UUID userId, Pageable pageable) {
    Page<Rating> ratings = ratingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    return ratings.map(this::mapToResponse);
  }

  @Override
  public RatingResponse getRating(UUID userId, UUID titleId) {
    Rating rating = ratingRepository.findByUserIdAndTitleId(userId, titleId)
        .orElseThrow(() -> new NotFoundException("Rating not found"));
    return mapToResponse(rating);
  }

  @Override
  public Page<RatingResponse> getTitleRatings(UUID titleId, Pageable pageable) {
    Page<Rating> ratings = ratingRepository.findByTitleIdOrderByCreatedAtDesc(titleId, pageable);
    return ratings.map(this::mapToResponse);
  }

  private RatingResponse mapToResponse(Rating rating) {
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

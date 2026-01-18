package com.filmreview.service;

import com.filmreview.dto.UpdateUserRequest;
import com.filmreview.dto.UserResponse;
import com.filmreview.entity.User;
import com.filmreview.exception.NotFoundException;
import com.filmreview.repository.RatingRepository;
import com.filmreview.repository.ReviewRepository;
import com.filmreview.repository.UserRepository;
import com.filmreview.repository.WatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RatingRepository ratingRepository;
  private final ReviewRepository reviewRepository;
  private final WatchlistRepository watchlistRepository;

  public UserService(
      UserRepository userRepository,
      RatingRepository ratingRepository,
      ReviewRepository reviewRepository,
      WatchlistRepository watchlistRepository) {
    this.userRepository = userRepository;
    this.ratingRepository = ratingRepository;
    this.reviewRepository = reviewRepository;
    this.watchlistRepository = watchlistRepository;
  }

  /**
   * Get current user profile with stats.
   */
  public UserResponse getCurrentUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    return mapToUserResponse(user, true); // true = include email (own profile)
  }

  /**
   * Get user profile by username with stats (public profile, no email).
   */
  public UserResponse getUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

    return mapToUserResponse(user, false); // false = don't include email (public profile)
  }

  /**
   * Update current user profile.
   */
  @Transactional
  public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    // Update only provided fields
    if (request.getDisplayName() != null) {
      user.setDisplayName(request.getDisplayName());
    }
    if (request.getBio() != null) {
      user.setBio(request.getBio());
    }
    if (request.getAvatarUrl() != null) {
      user.setAvatarUrl(request.getAvatarUrl());
    }

    User updatedUser = userRepository.save(user);
    return mapToUserResponse(updatedUser, true); // true = include email (own profile)
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

    // Fetch stats
    UserResponse.UserStats stats = new UserResponse.UserStats();
    stats.setRatingsCount(ratingRepository.countByUserId(user.getId()));
    stats.setReviewsCount(reviewRepository.countByUserId(user.getId()));
    stats.setWatchlistCount(watchlistRepository.countByUserId(user.getId()));
    response.setStats(stats);

    return response;
  }
}

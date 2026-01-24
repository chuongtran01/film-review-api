package com.filmreview.service;

import com.filmreview.dto.UpdateUserRequest;
import com.filmreview.dto.UserResponse;

import java.util.UUID;

/**
 * Interface for managing user profiles and operations.
 */
public interface UserService {

  /**
   * Get current user profile with stats.
   */
  UserResponse getCurrentUser(UUID userId);

  /**
   * Get user profile by username with stats (public profile, no email).
   */
  UserResponse getUserByUsername(String username);

  /**
   * Update current user profile.
   */
  UserResponse updateUser(UUID userId, UpdateUserRequest request);
}

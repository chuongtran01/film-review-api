package com.filmreview.service;

import com.filmreview.dto.AuthResponse;
import com.filmreview.dto.LoginRequest;
import com.filmreview.dto.RegisterRequest;

/**
 * Interface for authentication operations.
 */
public interface AuthService {

  /**
   * Register a new user.
   */
  AuthResponse register(RegisterRequest request);

  /**
   * Login with email and password.
   */
  AuthResponse login(LoginRequest request);

  /**
   * Refresh access token using refresh token.
   */
  AuthResponse refreshToken(String refreshToken);

  /**
   * Logout (invalidate refresh token).
   */
  void logout(String refreshToken);
}

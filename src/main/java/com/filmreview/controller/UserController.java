package com.filmreview.controller;

import com.filmreview.dto.UpdateUserRequest;
import com.filmreview.dto.UserResponse;
import com.filmreview.security.UserPrincipal;
import com.filmreview.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Get current user profile.
   * GET /api/v1/users/me
   */
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<UserResponse> getCurrentUser(
      @AuthenticationPrincipal UserPrincipal currentUser) {
    UserResponse response = userService.getCurrentUser(currentUser.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Get user profile by username (public).
   * GET /api/v1/users/{username}
   */
  @GetMapping("/{username}")
  public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
    UserResponse response = userService.getUserByUsername(username);
    return ResponseEntity.ok(response);
  }

  /**
   * Update current user profile.
   * PATCH /api/v1/users/me
   */
  @PatchMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<UserResponse> updateUser(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @Valid @RequestBody UpdateUserRequest request) {
    UserResponse response = userService.updateUser(currentUser.getId(), request);
    return ResponseEntity.ok(response);
  }
}

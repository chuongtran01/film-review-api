package com.filmreview.controller.admin;

import com.filmreview.entity.User;
import com.filmreview.security.UserPrincipal;
import com.filmreview.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Admin user management controller.
 * Handles user promotion/demotion to admin role.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminService adminService;

  public AdminUserController(AdminService adminService) {
    this.adminService = adminService;
  }

  @PostMapping("/{userId}/make-admin")
  public ResponseEntity<Map<String, Object>> makeUserAdmin(
      @PathVariable UUID userId,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    User user = adminService.promoteToAdmin(userId, currentUser);
    return ResponseEntity.ok(Map.of(
        "message", "User promoted to admin successfully",
        "userId", user.getId(),
        "username", user.getUsername()));
  }

  @PostMapping("/{userId}/remove-admin")
  public ResponseEntity<Map<String, Object>> removeUserAdmin(
      @PathVariable UUID userId,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    User user = adminService.removeAdminRole(userId, currentUser);
    return ResponseEntity.ok(Map.of(
        "message", "Admin role removed from user successfully",
        "userId", user.getId(),
        "username", user.getUsername()));
  }
}

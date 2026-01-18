package com.filmreview.controller.admin;

import com.filmreview.entity.Role;
import com.filmreview.security.UserPrincipal;
import com.filmreview.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin role management controller.
 * Handles role assignment and management.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

  private final RoleService roleService;

  public AdminRoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @PostMapping("/users/{userId}/roles/{roleName}")
  @PreAuthorize("hasAuthority('users.manage')")
  public ResponseEntity<Map<String, Object>> assignRole(
      @PathVariable UUID userId,
      @PathVariable String roleName) {
    roleService.assignRoleToUser(userId, roleName);
    return ResponseEntity.ok(Map.of(
        "message", "Role assigned successfully",
        "userId", userId,
        "roleName", roleName));
  }

  @DeleteMapping("/users/{userId}/roles/{roleName}")
  @PreAuthorize("hasAuthority('users.manage')")
  public ResponseEntity<Map<String, Object>> removeRole(
      @PathVariable UUID userId,
      @PathVariable String roleName,
      @AuthenticationPrincipal UserPrincipal currentUser) {
    roleService.removeRoleFromUser(userId, roleName, currentUser);
    return ResponseEntity.ok(Map.of(
        "message", "Role removed successfully",
        "userId", userId,
        "roleName", roleName));
  }

  @GetMapping("/users/{userId}/roles")
  @PreAuthorize("hasAuthority('users.view')")
  public ResponseEntity<Map<String, Object>> getUserRoles(@PathVariable UUID userId) {
    List<String> roles = roleService.getUserRoles(userId);
    return ResponseEntity.ok(Map.of(
        "userId", userId,
        "roles", roles));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('users.view')")
  public ResponseEntity<List<Map<String, Object>>> getAllRoles() {
    List<Role> roles = roleService.getAllRoles();
    List<Map<String, Object>> roleDtos = roles.stream()
        .map(role -> Map.of(
            "id", role.getId(),
            "name", role.getName(),
            "displayName", role.getDisplayName(),
            "description", role.getDescription(),
            "permissions", role.getPermissions().stream()
                .map(p -> p.getName())
                .collect(Collectors.toSet())))
        .collect(Collectors.toList());
    return ResponseEntity.ok(roleDtos);
  }
}

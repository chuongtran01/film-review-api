package com.filmreview.service;

import com.filmreview.entity.Role;
import com.filmreview.entity.UserRole;
import com.filmreview.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

/**
 * Interface for managing user roles.
 */
public interface RoleService {

  /**
   * Assign a role to a user.
   * Requires ADMIN role.
   */
  UserRole assignRoleToUser(UUID userId, String roleName);

  /**
   * Remove a role from a user.
   * Requires ADMIN role.
   * Cannot remove the last role from a user (must have at least USER role).
   * Cannot remove ADMIN role from yourself.
   */
  void removeRoleFromUser(UUID userId, String roleName, UserPrincipal currentUser);

  /**
   * Get all roles for a user.
   */
  List<String> getUserRoles(UUID userId);

  /**
   * Get all available roles.
   */
  List<Role> getAllRoles();
}

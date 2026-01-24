package com.filmreview.service;

import com.filmreview.entity.User;
import com.filmreview.security.UserPrincipal;

import java.util.UUID;

/**
 * Interface for admin operations.
 */
public interface AdminService {

  /**
   * Promote a user to admin role.
   * Only existing admins can promote other users.
   */
  User promoteToAdmin(UUID userId, UserPrincipal currentUser);

  /**
   * Remove admin role from a user.
   * Only existing admins can remove admin role from others.
   * Cannot remove admin role from yourself.
   */
  User removeAdminRole(UUID userId, UserPrincipal currentUser);

  /**
   * Check if a user is an admin (has ADMIN role).
   */
  boolean isAdmin(UUID userId);
}

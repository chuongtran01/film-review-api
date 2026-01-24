package com.filmreview.service;

import java.util.Set;
import java.util.UUID;

/**
 * Interface for loading and managing user permissions.
 */
public interface PermissionService {

  /**
   * Get all permission names for a user based on their roles.
   * 
   * @param userId The user ID
   * @return Set of permission names (e.g., "titles.create", "titles.delete")
   */
  Set<String> getUserPermissions(UUID userId);
}

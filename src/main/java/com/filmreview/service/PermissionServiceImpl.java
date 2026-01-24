package com.filmreview.service;

import com.filmreview.repository.RoleRepository;
import com.filmreview.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of PermissionService for loading and managing user
 * permissions.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

  private final UserRoleRepository userRoleRepository;
  private final RoleRepository roleRepository;

  public PermissionServiceImpl(
      UserRoleRepository userRoleRepository,
      RoleRepository roleRepository) {
    this.userRoleRepository = userRoleRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public Set<String> getUserPermissions(UUID userId) {
    // Get all role IDs for the user
    List<com.filmreview.entity.UserRole> userRoles = userRoleRepository.findByUserId(userId);
    if (userRoles.isEmpty()) {
      return new HashSet<>();
    }

    List<Integer> roleIds = userRoles.stream()
        .map(com.filmreview.entity.UserRole::getRoleId)
        .toList();

    // Get all permission names for these roles
    List<String> permissionNames = roleRepository.findPermissionNamesByRoleIds(roleIds);
    return new HashSet<>(permissionNames);
  }
}

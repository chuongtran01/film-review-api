package com.filmreview.service;

import com.filmreview.entity.Role;
import com.filmreview.entity.UserRole;
import com.filmreview.exception.BadRequestException;
import com.filmreview.repository.RoleRepository;
import com.filmreview.repository.UserRepository;
import com.filmreview.repository.UserRoleRepository;
import com.filmreview.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoleServiceImpl implements RoleService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;

  public RoleServiceImpl(
      UserRepository userRepository,
      RoleRepository roleRepository,
      UserRoleRepository userRoleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.userRoleRepository = userRoleRepository;
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public UserRole assignRoleToUser(UUID userId, String roleName) {
    // Verify user exists
    userRepository.findById(userId)
        .orElseThrow(() -> new BadRequestException("User not found"));

    Role role = roleRepository.findByName(roleName.toUpperCase())
        .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));

    // Check if user already has this role
    if (userRoleRepository.existsByUserIdAndRoleId(userId, role.getId())) {
      throw new BadRequestException("User already has role: " + roleName);
    }

    UserRole userRole = new UserRole();
    userRole.setUserId(userId);
    userRole.setRoleId(role.getId());
    return userRoleRepository.save(userRole);
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void removeRoleFromUser(UUID userId, String roleName, @AuthenticationPrincipal UserPrincipal currentUser) {
    Role role = roleRepository.findByName(roleName.toUpperCase())
        .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));

    // Prevent removing ADMIN role from yourself
    if (currentUser != null && currentUser.getId().equals(userId) && "ADMIN".equalsIgnoreCase(roleName)) {
      throw new BadRequestException("Cannot remove ADMIN role from yourself");
    }

    List<UserRole> userRoles = userRoleRepository.findByUserId(userId);

    // Ensure user has at least one role
    if (userRoles.size() <= 1) {
      throw new BadRequestException("Cannot remove the last role from a user");
    }

    userRoleRepository.deleteByUserIdAndRoleId(userId, role.getId());
  }

  @Override
  public List<String> getUserRoles(UUID userId) {
    return userRoleRepository.findRoleNamesByUserId(userId);
  }

  @Override
  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }
}

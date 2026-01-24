package com.filmreview.service;

import com.filmreview.entity.User;
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
public class AdminServiceImpl implements AdminService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;

  public AdminServiceImpl(
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
  public User promoteToAdmin(UUID userId, @AuthenticationPrincipal UserPrincipal currentUser) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BadRequestException("User not found"));

    // Check if user already has ADMIN role
    var adminRole = roleRepository.findByName("ADMIN")
        .orElseThrow(() -> new BadRequestException("ADMIN role not found in database"));

    if (userRoleRepository.existsByUserIdAndRoleId(userId, adminRole.getId())) {
      throw new BadRequestException("User already has ADMIN role");
    }

    // Assign ADMIN role
    UserRole userRole = new UserRole();
    userRole.setUserId(userId);
    userRole.setRoleId(adminRole.getId());
    userRoleRepository.save(userRole);

    return user;
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public User removeAdminRole(UUID userId, @AuthenticationPrincipal UserPrincipal currentUser) {
    if (currentUser != null && currentUser.getId().equals(userId)) {
      throw new BadRequestException("Cannot remove ADMIN role from yourself");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BadRequestException("User not found"));

    var adminRole = roleRepository.findByName("ADMIN")
        .orElseThrow(() -> new BadRequestException("ADMIN role not found in database"));

    // Check if user has ADMIN role
    if (!userRoleRepository.existsByUserIdAndRoleId(userId, adminRole.getId())) {
      throw new BadRequestException("User does not have ADMIN role");
    }

    // Ensure user has at least one role remaining
    List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
    if (userRoles.size() <= 1) {
      throw new BadRequestException("Cannot remove the last role from a user");
    }

    // Remove ADMIN role
    userRoleRepository.deleteByUserIdAndRoleId(userId, adminRole.getId());

    return user;
  }

  @Override
  public boolean isAdmin(UUID userId) {
    var adminRole = roleRepository.findByName("ADMIN");
    if (adminRole.isEmpty()) {
      return false;
    }
    return userRoleRepository.existsByUserIdAndRoleId(userId, adminRole.get().getId());
  }
}

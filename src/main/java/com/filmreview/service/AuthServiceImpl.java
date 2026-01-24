package com.filmreview.service;

import com.filmreview.dto.AuthResponse;
import com.filmreview.dto.LoginRequest;
import com.filmreview.dto.RegisterRequest;
import com.filmreview.entity.User;
import com.filmreview.exception.BadRequestException;
import com.filmreview.exception.UnauthorizedException;
import com.filmreview.repository.UserRepository;
import com.filmreview.repository.UserRoleRepository;
import com.filmreview.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final PermissionService permissionService;

  public AuthServiceImpl(
      UserRepository userRepository,
      UserRoleRepository userRoleRepository,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider tokenProvider,
      PermissionService permissionService) {
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
    this.permissionService = permissionService;
  }

  private List<String> getUserRoles(UUID userId) {
    List<String> roles = userRoleRepository.findRoleNamesByUserId(userId);
    // If user has no roles, assign default USER role
    if (roles.isEmpty()) {
      return List.of("USER");
    }
    return roles;
  }

  private List<String> getUserPermissions(UUID userId) {
    return new java.util.ArrayList<>(permissionService.getUserPermissions(userId));
  }

  @Override
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Email already exists");
    }

    // Generate username from email (part before @)
    String baseUsername = request.getEmail().split("@")[0];
    String username = baseUsername;
    int counter = 1;

    // Ensure username is unique
    while (userRepository.existsByUsername(username)) {
      username = baseUsername + counter;
      counter++;
    }

    // Create new user
    User user = new User();
    user.setUsername(username);
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setDisplayName(username); // Use generated username as display name initially
    user.setVerified(false);

    user = userRepository.save(user);

    // Assign default USER role to new user
    // This will be handled by the migration, but we ensure it here too
    List<String> roles = getUserRoles(user.getId());
    List<String> permissions = getUserPermissions(user.getId());

    // Generate tokens
    String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getEmail(), roles,
        permissions);
    String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail(), roles,
        permissions);

    return buildAuthResponse(user, accessToken, refreshToken);
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    // Find user by email
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    // Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid email or password");
    }

    // Update last active timestamp
    user.setLastActiveAt(LocalDateTime.now());
    userRepository.save(user);

    // Get user roles and permissions
    List<String> roles = getUserRoles(user.getId());
    List<String> permissions = getUserPermissions(user.getId());

    // Generate tokens
    String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getEmail(), roles,
        permissions);
    String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail(), roles,
        permissions);

    return buildAuthResponse(user, accessToken, refreshToken);
  }

  @Override
  public AuthResponse refreshToken(String refreshToken) {
    if (!tokenProvider.validateToken(refreshToken)) {
      throw new UnauthorizedException("Invalid or expired refresh token");
    }

    UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));

    // Get user roles and permissions (may have changed since token was issued)
    List<String> roles = getUserRoles(user.getId());
    List<String> permissions = getUserPermissions(user.getId());

    // Generate new access token
    String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getEmail(), roles,
        permissions);

    // Return new access token (refresh token remains the same)
    return buildAuthResponse(user, newAccessToken, refreshToken);
  }

  @Override
  public void logout(String refreshToken) {
    // In a stateless JWT system, logout is handled client-side by removing tokens
    // For enhanced security, you could maintain a token blacklist in Redis
    // For MVP, we'll just validate the token format
    if (refreshToken != null && !tokenProvider.validateToken(refreshToken)) {
      throw new UnauthorizedException("Invalid refresh token");
    }
  }

  private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
    AuthResponse.UserDto userDto = new AuthResponse.UserDto();
    userDto.setId(user.getId());
    userDto.setUsername(user.getUsername());
    userDto.setEmail(user.getEmail());
    userDto.setDisplayName(user.getDisplayName());
    userDto.setAvatarUrl(user.getAvatarUrl());
    userDto.setVerified(user.getVerified());
    userDto.setCreatedAt(user.getCreatedAt());

    AuthResponse response = new AuthResponse();
    response.setUser(userDto);
    response.setAccessToken(accessToken);
    response.setRefreshToken(refreshToken);

    return response;
  }
}

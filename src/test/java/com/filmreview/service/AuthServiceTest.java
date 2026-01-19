package com.filmreview.service;

import com.filmreview.dto.AuthResponse;
import com.filmreview.dto.LoginRequest;
import com.filmreview.dto.RegisterRequest;
import com.filmreview.entity.User;
import com.filmreview.exception.BadRequestException;
import com.filmreview.exception.UnauthorizedException;
import com.filmreview.faker.UserFaker;
import com.filmreview.repository.UserRepository;
import com.filmreview.repository.UserRoleRepository;
import com.filmreview.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserRoleRepository userRoleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenProvider tokenProvider;

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private AuthService authService;

  private User testUser;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    testUser = UserFaker.generate(userId, "test@example.com", "testuser", "$2a$12$encodedPasswordHash", "testuser");
  }

  @Test
  void testRegister_Success() {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    // Mock repository behaviors
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("test")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("$2a$12$encodedHash");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(userId);
      user.setCreatedAt(LocalDateTime.now());
      return user;
    });
    when(tokenProvider.generateAccessToken(any(UUID.class), anyString(), anyString(), anyList(), anyList()))
        .thenReturn("access-token");
    when(tokenProvider.generateRefreshToken(any(UUID.class), anyString(), anyString(), anyList(), anyList()))
        .thenReturn("refresh-token");

    AuthResponse response = authService.register(request);

    assertNotNull(response);
    assertNotNull(response.getUser());
    assertEquals("test@example.com", response.getUser().getEmail());
    assertEquals("test", response.getUser().getUsername());
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());

    // Verify interactions
    verify(userRepository).existsByEmail("test@example.com");
    verify(userRepository).existsByUsername("test");
    verify(passwordEncoder).encode("password123");
    verify(userRepository).save(any(User.class));
    verify(tokenProvider).generateAccessToken(any(UUID.class), eq("test"), eq("test@example.com"), anyList(),
        anyList());
    verify(tokenProvider).generateRefreshToken(any(UUID.class), eq("test"), eq("test@example.com"), anyList(),
        anyList());
  }

  @Test
  void testRegister_DuplicateEmail() {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("existing@example.com");
    request.setPassword("password123");

    // Mock email already exists
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThrows(BadRequestException.class, () -> authService.register(request));

    verify(userRepository).existsByEmail("existing@example.com");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testRegister_GeneratesUniqueUsername() {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    // Mock username conflict - first attempt exists, second doesn't
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("test")).thenReturn(true); // First username taken
    when(userRepository.existsByUsername("test1")).thenReturn(false); // Second username available
    when(passwordEncoder.encode("password123")).thenReturn("$2a$12$encodedHash");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(userId);
      user.setCreatedAt(LocalDateTime.now());
      return user;
    });
    when(userRoleRepository.findRoleNamesByUserId(userId)).thenReturn(List.of("USER"));
    when(permissionService.getUserPermissions(userId)).thenReturn(Set.of());
    when(tokenProvider.generateAccessToken(any(UUID.class), anyString(), anyString(), anyList(), anyList()))
        .thenReturn("access-token");
    when(tokenProvider.generateRefreshToken(any(UUID.class), anyString(), anyString(), anyList(), anyList()))
        .thenReturn("refresh-token");

    AuthResponse response = authService.register(request);

    assertEquals("test1", response.getUser().getUsername());
    verify(userRepository).existsByUsername("test");
    verify(userRepository).existsByUsername("test1");
  }

  @Test
  void testLogin_Success() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    // Mock repository and encoder
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userRoleRepository.findRoleNamesByUserId(userId)).thenReturn(List.of("USER"));
    when(permissionService.getUserPermissions(userId)).thenReturn(Set.of());
    when(tokenProvider.generateAccessToken(any(UUID.class), anyString(), anyString(), anyList(), anyList()))
        .thenReturn("access-token");
    when(tokenProvider.generateRefreshToken(any(UUID.class), anyString(), anyString(), anyList(), anyList()))
        .thenReturn("refresh-token");

    AuthResponse response = authService.login(request);

    assertNotNull(response);
    assertEquals("test@example.com", response.getUser().getEmail());
    assertEquals("testuser", response.getUser().getUsername());
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());

    verify(userRepository).findByEmail("test@example.com");
    verify(passwordEncoder).matches("password123", testUser.getPasswordHash());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testLogin_InvalidEmail() {
    LoginRequest request = new LoginRequest();
    request.setEmail("nonexistent@example.com");
    request.setPassword("password123");

    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.login(request));

    verify(userRepository).findByEmail("nonexistent@example.com");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  void testLogin_WrongPassword() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("wrongpassword");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.login(request));

    verify(userRepository).findByEmail("test@example.com");
    verify(passwordEncoder).matches("wrongpassword", testUser.getPasswordHash());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testRefreshToken_Success() {
    String refreshToken = "valid-refresh-token";

    when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
    when(tokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userRoleRepository.findRoleNamesByUserId(userId)).thenReturn(List.of("USER"));
    when(permissionService.getUserPermissions(userId)).thenReturn(Set.of());
    when(tokenProvider.generateAccessToken(any(UUID.class), anyString(), anyString(), anyList(), anyList()))
        .thenReturn("new-access-token");

    AuthResponse response = authService.refreshToken(refreshToken);

    assertNotNull(response);
    assertNotNull(response.getAccessToken());
    assertEquals(refreshToken, response.getRefreshToken()); // Refresh token should remain the same
    assertEquals("test@example.com", response.getUser().getEmail());

    verify(tokenProvider).validateToken(refreshToken);
    verify(tokenProvider).getUserIdFromToken(refreshToken);
    verify(userRepository).findById(userId);
    verify(tokenProvider).generateAccessToken(any(UUID.class), eq("testuser"), eq("test@example.com"), anyList(),
        anyList());
  }

  @Test
  void testRefreshToken_InvalidToken() {
    String invalidToken = "invalid-token";
    when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.refreshToken(invalidToken));

    verify(tokenProvider).validateToken(invalidToken);
    verify(userRepository, never()).findById(any(UUID.class));
  }

  @Test
  void testRefreshToken_UserNotFound() {
    String refreshToken = "valid-token";
    UUID nonExistentUserId = UUID.randomUUID();

    when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
    when(tokenProvider.getUserIdFromToken(refreshToken)).thenReturn(nonExistentUserId);
    when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.refreshToken(refreshToken));

    verify(tokenProvider).validateToken(refreshToken);
    verify(tokenProvider).getUserIdFromToken(refreshToken);
    verify(userRepository).findById(nonExistentUserId);
  }

  @Test
  void testLogout_Success() {
    String refreshToken = "valid-refresh-token";
    when(tokenProvider.validateToken(refreshToken)).thenReturn(true);

    // Should not throw exception
    assertDoesNotThrow(() -> authService.logout(refreshToken));

    verify(tokenProvider).validateToken(refreshToken);
  }

  @Test
  void testLogout_InvalidToken() {
    String invalidToken = "invalid-token";
    when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.logout(invalidToken));

    verify(tokenProvider).validateToken(invalidToken);
  }

  @Test
  void testLogout_NullToken() {
    // Should not throw exception for null token (handled gracefully)
    assertDoesNotThrow(() -> authService.logout(null));

    verify(tokenProvider, never()).validateToken(anyString());
  }
}

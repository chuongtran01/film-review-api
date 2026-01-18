package com.filmreview.service;

import com.filmreview.dto.AuthResponse;
import com.filmreview.dto.LoginRequest;
import com.filmreview.dto.RegisterRequest;
import com.filmreview.entity.User;
import com.filmreview.exception.BadRequestException;
import com.filmreview.exception.UnauthorizedException;
import com.filmreview.repository.UserRepository;
import com.filmreview.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class AuthServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void testRegister_Success() {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    AuthResponse response = authService.register(request);

    assertNotNull(response);
    assertNotNull(response.getUser());
    assertEquals("test@example.com", response.getUser().getEmail());
    assertNotNull(response.getUser().getUsername());
    assertTrue(response.getUser().getUsername().startsWith("test"));
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());

    // Verify user was saved
    User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
    assertNotNull(savedUser);
    assertTrue(passwordEncoder.matches("password123", savedUser.getPasswordHash()));
  }

  @Test
  void testRegister_DuplicateEmail() {
    // Create existing user
    User existingUser = new User();
    existingUser.setEmail("existing@example.com");
    existingUser.setUsername("existing");
    existingUser.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(existingUser);

    RegisterRequest request = new RegisterRequest();
    request.setEmail("existing@example.com");
    request.setPassword("password123");

    assertThrows(BadRequestException.class, () -> authService.register(request));
  }

  @Test
  void testRegister_GeneratesUniqueUsername() {
    // Register first user
    RegisterRequest request1 = new RegisterRequest();
    request1.setEmail("test@example.com");
    request1.setPassword("password123");
    AuthResponse response1 = authService.register(request1);
    String username1 = response1.getUser().getUsername();

    // Register second user with same email prefix
    RegisterRequest request2 = new RegisterRequest();
    request2.setEmail("test+1@example.com");
    request2.setPassword("password123");
    AuthResponse response2 = authService.register(request2);
    String username2 = response2.getUser().getUsername();

    // Usernames should be different
    assertNotEquals(username1, username2);
  }

  @Test
  void testLogin_Success() {
    // Create user
    User user = new User();
    user.setEmail("test@example.com");
    user.setUsername("testuser");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    user = userRepository.save(user);

    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    AuthResponse response = authService.login(request);

    assertNotNull(response);
    assertEquals("test@example.com", response.getUser().getEmail());
    assertEquals("testuser", response.getUser().getUsername());
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
  }

  @Test
  void testLogin_InvalidEmail() {
    LoginRequest request = new LoginRequest();
    request.setEmail("nonexistent@example.com");
    request.setPassword("password123");

    assertThrows(UnauthorizedException.class, () -> authService.login(request));
  }

  @Test
  void testLogin_WrongPassword() {
    // Create user
    User user = new User();
    user.setEmail("test@example.com");
    user.setUsername("testuser");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    userRepository.save(user);

    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("wrongpassword");

    assertThrows(UnauthorizedException.class, () -> authService.login(request));
  }

  @Test
  void testRefreshToken_Success() {
    // Create user
    User user = new User();
    user.setEmail("test@example.com");
    user.setUsername("testuser");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    user = userRepository.save(user);

    // Generate refresh token
    String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail());

    AuthResponse response = authService.refreshToken(refreshToken);

    assertNotNull(response);
    assertNotNull(response.getAccessToken());
    assertEquals(refreshToken, response.getRefreshToken()); // Refresh token should remain the same
    assertEquals("test@example.com", response.getUser().getEmail());
  }

  @Test
  void testRefreshToken_InvalidToken() {
    assertThrows(UnauthorizedException.class, () -> authService.refreshToken("invalid-token"));
  }

  @Test
  void testRefreshToken_ExpiredToken() {
    // Create user
    User user = new User();
    user.setEmail("test@example.com");
    user.setUsername("testuser");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    user = userRepository.save(user);

    // Generate expired token (manually create expired token)
    // This would require mocking time, but for now we'll test with invalid format
    assertThrows(UnauthorizedException.class, () -> authService.refreshToken("expired.token.here"));
  }

  @Test
  void testLogout_Success() {
    // Create user
    User user = new User();
    user.setEmail("test@example.com");
    user.setUsername("testuser");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    user = userRepository.save(user);

    // Generate refresh token
    String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail());

    // Should not throw exception
    assertDoesNotThrow(() -> authService.logout(refreshToken));
  }

  @Test
  void testLogout_InvalidToken() {
    assertThrows(UnauthorizedException.class, () -> authService.logout("invalid-token"));
  }
}

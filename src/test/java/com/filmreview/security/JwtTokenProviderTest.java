package com.filmreview.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class JwtTokenProviderTest {

  @Autowired
  private JwtTokenProvider tokenProvider;

  private UUID userId;
  private String username;
  private String email;
  private List<String> roles;
  private List<String> permissions;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    username = "testuser";
    email = "test@example.com";
    roles = List.of("USER");
    permissions = List.of();
  }

  @Test
  void testGenerateAccessToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email, roles, permissions);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void testGenerateRefreshToken() {
    String token = tokenProvider.generateRefreshToken(userId, username, email, roles, permissions);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void testGetUserIdFromToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email, roles, permissions);
    UUID extractedUserId = tokenProvider.getUserIdFromToken(token);

    assertEquals(userId, extractedUserId);
  }

  @Test
  void testGetUsernameFromToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email, roles, permissions);
    String extractedUsername = tokenProvider.getUsernameFromToken(token);

    assertEquals(username, extractedUsername);
  }

  @Test
  void testGetEmailFromToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email, roles, permissions);
    String extractedEmail = tokenProvider.getEmailFromToken(token);

    assertEquals(email, extractedEmail);
  }

  @Test
  void testGetRolesFromToken() {
    List<String> testRoles = List.of("USER", "ADMIN");
    String token = tokenProvider.generateAccessToken(userId, username, email, testRoles, permissions);
    List<String> extractedRoles = tokenProvider.getRolesFromToken(token);

    assertNotNull(extractedRoles);
    assertEquals(2, extractedRoles.size());
    assertTrue(extractedRoles.contains("USER"));
    assertTrue(extractedRoles.contains("ADMIN"));
  }

  @Test
  void testGetPermissionsFromToken() {
    List<String> testPermissions = List.of("titles.create", "titles.update");
    String token = tokenProvider.generateAccessToken(userId, username, email, roles, testPermissions);
    List<String> extractedPermissions = tokenProvider.getPermissionsFromToken(token);

    assertNotNull(extractedPermissions);
    assertEquals(2, extractedPermissions.size());
    assertTrue(extractedPermissions.contains("titles.create"));
    assertTrue(extractedPermissions.contains("titles.update"));
  }

  @Test
  void testValidateToken_ValidToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email, roles, permissions);
    assertTrue(tokenProvider.validateToken(token));
  }

  @Test
  void testValidateToken_InvalidToken() {
    assertFalse(tokenProvider.validateToken("invalid.token.here"));
  }

  @Test
  void testValidateToken_EmptyToken() {
    assertFalse(tokenProvider.validateToken(""));
  }

  @Test
  void testValidateToken_NullToken() {
    assertFalse(tokenProvider.validateToken(null));
  }

  @Test
  void testTokenExpiration() {
    String accessToken = tokenProvider.generateAccessToken(userId, username, email, roles, permissions);
    String refreshToken = tokenProvider.generateRefreshToken(userId, username, email, roles, permissions);

    // Both tokens should be valid immediately after generation
    assertTrue(tokenProvider.validateToken(accessToken));
    assertTrue(tokenProvider.validateToken(refreshToken));
  }

  @Test
  void testTokenContainsCorrectClaims() {
    String token = tokenProvider.generateAccessToken(userId, username, email, roles, permissions);

    UUID extractedUserId = tokenProvider.getUserIdFromToken(token);
    String extractedUsername = tokenProvider.getUsernameFromToken(token);
    String extractedEmail = tokenProvider.getEmailFromToken(token);

    assertEquals(userId, extractedUserId);
    assertEquals(username, extractedUsername);
    assertEquals(email, extractedEmail);
  }
}

package com.filmreview.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    username = "testuser";
    email = "test@example.com";
  }

  @Test
  void testGenerateAccessToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void testGenerateRefreshToken() {
    String token = tokenProvider.generateRefreshToken(userId, username, email);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void testGetUserIdFromToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email);
    UUID extractedUserId = tokenProvider.getUserIdFromToken(token);

    assertEquals(userId, extractedUserId);
  }

  @Test
  void testGetUsernameFromToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email);
    String extractedUsername = tokenProvider.getUsernameFromToken(token);

    assertEquals(username, extractedUsername);
  }

  @Test
  void testGetEmailFromToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email);
    String extractedEmail = tokenProvider.getEmailFromToken(token);

    assertEquals(email, extractedEmail);
  }

  @Test
  void testValidateToken_ValidToken() {
    String token = tokenProvider.generateAccessToken(userId, username, email);
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
    String accessToken = tokenProvider.generateAccessToken(userId, username, email);
    String refreshToken = tokenProvider.generateRefreshToken(userId, username, email);

    // Both tokens should be valid immediately after generation
    assertTrue(tokenProvider.validateToken(accessToken));
    assertTrue(tokenProvider.validateToken(refreshToken));
  }

  @Test
  void testTokenContainsCorrectClaims() {
    String token = tokenProvider.generateAccessToken(userId, username, email);

    UUID extractedUserId = tokenProvider.getUserIdFromToken(token);
    String extractedUsername = tokenProvider.getUsernameFromToken(token);
    String extractedEmail = tokenProvider.getEmailFromToken(token);

    assertEquals(userId, extractedUserId);
    assertEquals(username, extractedUsername);
    assertEquals(email, extractedEmail);
  }
}

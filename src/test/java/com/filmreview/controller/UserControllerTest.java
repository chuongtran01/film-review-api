package com.filmreview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmreview.dto.UpdateUserRequest;
import com.filmreview.entity.User;
import com.filmreview.faker.UserFaker;
import com.filmreview.repository.UserRepository;
import com.filmreview.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "dev", "test" })
@Transactional
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  private User testUser;
  private String accessToken;

  @BeforeEach
  void setUp() {
    // Create test user using faker
    testUser = UserFaker.generate("test@example.com", "testuser");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);

    // Generate JWT token
    List<String> roles = List.of("USER");
    List<String> permissions = List.of();
    accessToken = tokenProvider.generateAccessToken(
        testUser.getId(),
        testUser.getUsername(),
        testUser.getEmail(),
        roles,
        permissions);
  }

  @Test
  void testGetCurrentUser_Success() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com")) // Email included for own profile
        .andExpect(jsonPath("$.stats").exists())
        .andExpect(jsonPath("$.stats.ratingsCount").value(0))
        .andExpect(jsonPath("$.stats.reviewsCount").value(0))
        .andExpect(jsonPath("$.stats.watchlistCount").value(0));
  }

  @Test
  void testGetCurrentUser_Unauthorized() throws Exception {
    // When no authentication is provided, @PreAuthorize returns 403 Forbidden
    mockMvc.perform(get("/api/v1/users/me"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testGetUserByUsername_Success() throws Exception {
    mockMvc.perform(get("/api/v1/users/{username}", "testuser"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").doesNotExist()) // Email NOT included in public profile
        .andExpect(jsonPath("$.stats").exists());
  }

  @Test
  void testGetUserByUsername_NotFound() throws Exception {
    mockMvc.perform(get("/api/v1/users/{username}", "nonexistent"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }

  @Test
  void testGetUserByUsername_PublicAccess() throws Exception {
    // Should work without authentication
    mockMvc.perform(get("/api/v1/users/{username}", "testuser"))
        .andExpect(status().isOk());
  }

  @Test
  void testUpdateUser_Success() throws Exception {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("Updated Name");
    request.setBio("Updated bio");
    request.setAvatarUrl("https://example.com/avatar.jpg");

    mockMvc.perform(patch("/api/v1/users/me")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Updated Name"))
        .andExpect(jsonPath("$.bio").value("Updated bio"))
        .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"))
        .andExpect(jsonPath("$.username").value("testuser")); // Username unchanged
  }

  @Test
  void testUpdateUser_PartialUpdate() throws Exception {
    // Set initial values
    testUser.setDisplayName("Initial Name");
    testUser.setBio("Initial bio");
    userRepository.save(testUser);

    // Update only display name
    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("Updated Name");

    mockMvc.perform(patch("/api/v1/users/me")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Updated Name"))
        .andExpect(jsonPath("$.bio").value("Initial bio")); // Bio unchanged
  }

  @Test
  void testUpdateUser_InvalidDisplayName_TooLong() throws Exception {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("a".repeat(101)); // Exceeds 100 character limit

    mockMvc.perform(patch("/api/v1/users/me")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void testUpdateUser_InvalidBio_TooLong() throws Exception {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setBio("a".repeat(2001)); // Exceeds 2000 character limit

    mockMvc.perform(patch("/api/v1/users/me")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void testUpdateUser_InvalidAvatarUrl_TooLong() throws Exception {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setAvatarUrl("a".repeat(501)); // Exceeds 500 character limit

    mockMvc.perform(patch("/api/v1/users/me")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void testUpdateUser_Unauthorized() throws Exception {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("Updated Name");

    // When no authentication is provided, @PreAuthorize returns 403 Forbidden
    mockMvc.perform(patch("/api/v1/users/me")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUpdateUser_EmptyRequest() throws Exception {
    UpdateUserRequest request = new UpdateUserRequest();
    // All fields null

    mockMvc.perform(patch("/api/v1/users/me")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("testuser")); // User unchanged
  }

  @Test
  void testGetCurrentUser_WithStats() throws Exception {
    // Get current user stats (should be 0)
    mockMvc.perform(get("/api/v1/users/me")
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats.ratingsCount").value(0))
        .andExpect(jsonPath("$.stats.reviewsCount").value(0))
        .andExpect(jsonPath("$.stats.watchlistCount").value(0));
  }
}

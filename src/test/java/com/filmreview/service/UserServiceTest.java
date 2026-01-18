package com.filmreview.service;

import com.filmreview.dto.UpdateUserRequest;
import com.filmreview.dto.UserResponse;
import com.filmreview.entity.User;
import com.filmreview.exception.NotFoundException;
import com.filmreview.repository.UserRepository;
import com.filmreview.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({ "dev", "test" })
@Transactional
class UserServiceTest {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private TestDataUtil testDataUtil;
  private User testUser;

  @BeforeEach
  void setUp() {
    testDataUtil = new TestDataUtil(jdbcTemplate, passwordEncoder, userRepository);
    testDataUtil.cleanup();

    testUser = testDataUtil.createAndSaveUser("test@example.com", "testuser");
  }

  @Test
  void testGetCurrentUser_Success() {
    UserResponse response = userService.getCurrentUser(testUser.getId());

    assertNotNull(response);
    assertEquals(testUser.getId(), response.getId());
    assertEquals("testuser", response.getUsername());
    assertEquals("test@example.com", response.getEmail()); // Email included for own profile
    assertNotNull(response.getStats());
    assertEquals(0L, response.getStats().getRatingsCount());
    assertEquals(0L, response.getStats().getReviewsCount());
    assertEquals(0L, response.getStats().getWatchlistCount());
  }

  @Test
  void testGetCurrentUser_NotFound() {
    assertThrows(NotFoundException.class, () -> userService.getCurrentUser(UUID.randomUUID()));
  }

  @Test
  void testGetUserByUsername_Success() {
    UserResponse response = userService.getUserByUsername("testuser");

    assertNotNull(response);
    assertEquals(testUser.getId(), response.getId());
    assertEquals("testuser", response.getUsername());
    assertNull(response.getEmail()); // Email NOT included for public profile
    assertNotNull(response.getStats());
  }

  @Test
  void testGetUserByUsername_NotFound() {
    assertThrows(NotFoundException.class, () -> userService.getUserByUsername("nonexistent"));
  }

  @Test
  void testGetUserByUsername_EmailNotIncluded() {
    UserResponse response = userService.getUserByUsername("testuser");

    // Public profile should not include email
    assertNull(response.getEmail());
  }

  @Test
  void testUpdateUser_AllFields() {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("Updated Name");
    request.setBio("Updated bio");
    request.setAvatarUrl("https://example.com/avatar.jpg");

    UserResponse response = userService.updateUser(testUser.getId(), request);

    assertNotNull(response);
    assertEquals("Updated Name", response.getDisplayName());
    assertEquals("Updated bio", response.getBio());
    assertEquals("https://example.com/avatar.jpg", response.getAvatarUrl());
    assertEquals("testuser", response.getUsername()); // Username unchanged
  }

  @Test
  void testUpdateUser_PartialUpdate() {
    // Set initial values
    testUser.setDisplayName("Initial Name");
    testUser.setBio("Initial bio");
    userRepository.save(testUser);

    // Update only display name
    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("Updated Name");

    UserResponse response = userService.updateUser(testUser.getId(), request);

    assertEquals("Updated Name", response.getDisplayName());
    assertEquals("Initial bio", response.getBio()); // Bio unchanged
    assertNull(response.getAvatarUrl()); // Avatar unchanged
  }

  @Test
  void testUpdateUser_OnlyBio() {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setBio("New bio text");

    UserResponse response = userService.updateUser(testUser.getId(), request);

    assertNull(response.getDisplayName()); // Display name unchanged
    assertEquals("New bio text", response.getBio());
    assertNull(response.getAvatarUrl()); // Avatar unchanged
  }

  @Test
  void testUpdateUser_OnlyAvatar() {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setAvatarUrl("https://example.com/new-avatar.jpg");

    UserResponse response = userService.updateUser(testUser.getId(), request);

    assertNull(response.getDisplayName()); // Display name unchanged
    assertNull(response.getBio()); // Bio unchanged
    assertEquals("https://example.com/new-avatar.jpg", response.getAvatarUrl());
  }

  @Test
  void testUpdateUser_EmptyRequest() {
    UpdateUserRequest request = new UpdateUserRequest();
    // All fields null

    UserResponse response = userService.updateUser(testUser.getId(), request);

    // Should not throw exception, user unchanged
    assertNotNull(response);
    assertEquals("testuser", response.getUsername());
  }

  @Test
  void testUpdateUser_NotFound() {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("Updated Name");

    assertThrows(NotFoundException.class, () -> userService.updateUser(UUID.randomUUID(), request));
  }

  @Test
  void testGetCurrentUser_WithStats() {
    // Note: We can't actually create ratings/reviews/watchlist items yet
    // as those services aren't fully implemented. But the count methods
    // should return 0, which is fine for now.

    UserResponse response = userService.getCurrentUser(testUser.getId());

    assertNotNull(response.getStats());
    // Stats should be 0 since we haven't implemented full review/watchlist creation
    // yet
    assertEquals(0L, response.getStats().getRatingsCount());
    assertEquals(0L, response.getStats().getReviewsCount());
    assertEquals(0L, response.getStats().getWatchlistCount());
  }
}

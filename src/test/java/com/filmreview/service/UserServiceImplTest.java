package com.filmreview.service;

import com.filmreview.dto.UpdateUserRequest;
import com.filmreview.dto.UserResponse;
import com.filmreview.entity.User;
import com.filmreview.exception.NotFoundException;
import com.filmreview.faker.UserFaker;
import com.filmreview.repository.RatingRepository;
import com.filmreview.repository.ReviewRepository;
import com.filmreview.repository.UserRepository;
import com.filmreview.repository.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RatingRepository ratingRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private WatchlistRepository watchlistRepository;

  @InjectMocks
  private UserServiceImpl userService;

  private User testUser;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    testUser = UserFaker.generate(userId, "test@example.com", "testuser", "testuser");
  }

  @Test
  void testGetCurrentUser_Success() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(ratingRepository.countByUserId(userId)).thenReturn(0L);
    when(reviewRepository.countByUserId(userId)).thenReturn(0L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(0L);

    UserResponse response = userService.getCurrentUser(userId);

    assertNotNull(response);
    assertEquals(userId, response.getId());
    assertEquals("testuser", response.getUsername());
    assertEquals("test@example.com", response.getEmail()); // Email included for own profile
    assertNotNull(response.getStats());
    assertEquals(0L, response.getStats().getRatingsCount());
    assertEquals(0L, response.getStats().getReviewsCount());
    assertEquals(0L, response.getStats().getWatchlistCount());

    verify(userRepository).findById(userId);
    verify(ratingRepository).countByUserId(userId);
    verify(reviewRepository).countByUserId(userId);
    verify(watchlistRepository).countByUserId(userId);
  }

  @Test
  void testGetCurrentUser_NotFound() {
    UUID nonExistentId = UUID.randomUUID();
    when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.getCurrentUser(nonExistentId));

    verify(userRepository).findById(nonExistentId);
  }

  @Test
  void testGetUserByUsername_Success() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(ratingRepository.countByUserId(userId)).thenReturn(5L);
    when(reviewRepository.countByUserId(userId)).thenReturn(3L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(10L);

    UserResponse response = userService.getUserByUsername("testuser");

    assertNotNull(response);
    assertEquals(userId, response.getId());
    assertEquals("testuser", response.getUsername());
    assertNull(response.getEmail()); // Email NOT included for public profile
    assertNotNull(response.getStats());
    assertEquals(5L, response.getStats().getRatingsCount());
    assertEquals(3L, response.getStats().getReviewsCount());
    assertEquals(10L, response.getStats().getWatchlistCount());

    verify(userRepository).findByUsername("testuser");
    verify(ratingRepository).countByUserId(userId);
    verify(reviewRepository).countByUserId(userId);
    verify(watchlistRepository).countByUserId(userId);
  }

  @Test
  void testGetUserByUsername_NotFound() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.getUserByUsername("nonexistent"));

    verify(userRepository).findByUsername("nonexistent");
  }

  @Test
  void testGetUserByUsername_EmailNotIncluded() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(ratingRepository.countByUserId(userId)).thenReturn(0L);
    when(reviewRepository.countByUserId(userId)).thenReturn(0L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(0L);

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

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setUpdatedAt(LocalDateTime.now());
      return user;
    });
    when(ratingRepository.countByUserId(userId)).thenReturn(0L);
    when(reviewRepository.countByUserId(userId)).thenReturn(0L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(0L);

    UserResponse response = userService.updateUser(userId, request);

    assertNotNull(response);
    assertEquals("Updated Name", response.getDisplayName());
    assertEquals("Updated bio", response.getBio());
    assertEquals("https://example.com/avatar.jpg", response.getAvatarUrl());
    assertEquals("testuser", response.getUsername()); // Username unchanged

    verify(userRepository).findById(userId);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testUpdateUser_PartialUpdate() {
    // Set initial values
    testUser.setDisplayName("Initial Name");
    testUser.setBio("Initial bio");
    testUser.setAvatarUrl(null); // Ensure avatarUrl is null for this test

    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("Updated Name");

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setUpdatedAt(LocalDateTime.now());
      return user;
    });
    when(ratingRepository.countByUserId(userId)).thenReturn(0L);
    when(reviewRepository.countByUserId(userId)).thenReturn(0L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(0L);

    UserResponse response = userService.updateUser(userId, request);

    assertEquals("Updated Name", response.getDisplayName());
    assertEquals("Initial bio", response.getBio()); // Bio unchanged
    assertNull(response.getAvatarUrl()); // Avatar unchanged

    verify(userRepository).findById(userId);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testUpdateUser_OnlyBio() {
    // Ensure avatarUrl is null for this test
    testUser.setAvatarUrl(null);
    testUser.setBio(null);

    UpdateUserRequest request = new UpdateUserRequest();
    request.setBio("New bio text");

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      // The service modifies the user object, so we return it as-is
      user.setUpdatedAt(LocalDateTime.now());
      return user;
    });
    when(ratingRepository.countByUserId(userId)).thenReturn(0L);
    when(reviewRepository.countByUserId(userId)).thenReturn(0L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(0L);

    UserResponse response = userService.updateUser(userId, request);

    // After update, bio should be set, displayName should remain unchanged
    assertEquals("New bio text", response.getBio());
    assertEquals("testuser", response.getDisplayName()); // Display name unchanged (was "testuser" initially)
    assertNull(response.getAvatarUrl()); // Avatar unchanged
  }

  @Test
  void testUpdateUser_OnlyAvatar() {
    // Ensure bio is null for this test
    testUser.setAvatarUrl(null);
    testUser.setBio(null);

    UpdateUserRequest request = new UpdateUserRequest();
    request.setAvatarUrl("https://example.com/new-avatar.jpg");

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      // The service modifies the user object, so we return it as-is
      user.setUpdatedAt(LocalDateTime.now());
      return user;
    });
    when(ratingRepository.countByUserId(userId)).thenReturn(0L);
    when(reviewRepository.countByUserId(userId)).thenReturn(0L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(0L);

    UserResponse response = userService.updateUser(userId, request);

    assertEquals("https://example.com/new-avatar.jpg", response.getAvatarUrl());
    assertEquals("testuser", response.getDisplayName()); // Display name unchanged (was "testuser" initially)
    assertNull(response.getBio()); // Bio unchanged
  }

  @Test
  void testUpdateUser_EmptyRequest() {
    UpdateUserRequest request = new UpdateUserRequest();
    // All fields null

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(ratingRepository.countByUserId(userId)).thenReturn(0L);
    when(reviewRepository.countByUserId(userId)).thenReturn(0L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(0L);

    UserResponse response = userService.updateUser(userId, request);

    // Should not throw exception, user unchanged
    assertNotNull(response);
    assertEquals("testuser", response.getUsername());
  }

  @Test
  void testUpdateUser_NotFound() {
    UUID nonExistentId = UUID.randomUUID();
    UpdateUserRequest request = new UpdateUserRequest();
    request.setDisplayName("Updated Name");

    when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.updateUser(nonExistentId, request));

    verify(userRepository).findById(nonExistentId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testGetCurrentUser_WithStats() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(ratingRepository.countByUserId(userId)).thenReturn(5L);
    when(reviewRepository.countByUserId(userId)).thenReturn(3L);
    when(watchlistRepository.countByUserId(userId)).thenReturn(10L);

    UserResponse response = userService.getCurrentUser(userId);

    assertNotNull(response.getStats());
    assertEquals(5L, response.getStats().getRatingsCount());
    assertEquals(3L, response.getStats().getReviewsCount());
    assertEquals(10L, response.getStats().getWatchlistCount());
  }
}

package com.filmreview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmreview.dto.WatchlistRequest;
import com.filmreview.dto.WatchlistUpdateRequest;
import com.filmreview.entity.Title;
import com.filmreview.entity.Watchlist;
import com.filmreview.faker.UserFaker;
import com.filmreview.repository.TitleRepository;
import com.filmreview.repository.UserRepository;
import com.filmreview.repository.WatchlistRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "dev", "test" })
@Transactional
class WatchlistControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TitleRepository titleRepository;

  @Autowired
  private WatchlistRepository watchlistRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  private com.filmreview.entity.User testUser;
  private Title testTitle;
  private String accessToken;

  @BeforeEach
  void setUp() {
    // Create test user
    testUser = UserFaker.generate("test@example.com", "testuser");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);

    // Create test title
    testTitle = new Title();
    testTitle.setTitle("Test Movie");
    testTitle.setTmdbId(12345);
    testTitle.setType(Title.TitleType.movie);
    testTitle.setSlug("test-movie");
    testTitle = titleRepository.save(testTitle);

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
  void testGetUserWatchlist_Success() throws Exception {
    // Arrange
    Watchlist watchlist = new Watchlist();
    watchlist.setUserId(testUser.getId());
    watchlist.setTitleId(testTitle.getId());
    watchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
    watchlistRepository.save(watchlist);

    // Act & Assert
    mockMvc.perform(get("/api/v1/watchlist")
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].user_id").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.content[0].title_id").value(testTitle.getId().toString()))
        .andExpect(jsonPath("$.content[0].status").value("want_to_watch"));
  }

  @Test
  void testGetUserWatchlist_WithStatusFilter() throws Exception {
    // Arrange
    Watchlist watchlist1 = new Watchlist();
    watchlist1.setUserId(testUser.getId());
    watchlist1.setTitleId(testTitle.getId());
    watchlist1.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
    watchlistRepository.save(watchlist1);

    Title title2 = new Title();
    title2.setTitle("Test Movie 2");
    title2.setTmdbId(12346);
    title2.setType(Title.TitleType.movie);
    title2.setSlug("test-movie-2");
    title2 = titleRepository.save(title2);

    Watchlist watchlist2 = new Watchlist();
    watchlist2.setUserId(testUser.getId());
    watchlist2.setTitleId(title2.getId());
    watchlist2.setStatus(Watchlist.WatchlistStatus.WATCHING);
    watchlistRepository.save(watchlist2);

    // Act & Assert
    mockMvc.perform(get("/api/v1/watchlist")
        .param("status", "WANT_TO_WATCH")
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].status").value("want_to_watch")); // JSON uses lowercase via @JsonValue
  }

  @Test
  void testGetUserWatchlist_Empty() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/v1/watchlist")
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  @Test
  void testAddToWatchlist_Success() throws Exception {
    // Arrange
    WatchlistRequest request = new WatchlistRequest();
    request.setTitleId(testTitle.getId());
    request.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    // Act & Assert
    mockMvc.perform(post("/api/v1/watchlist")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.user_id").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.title_id").value(testTitle.getId().toString()))
        .andExpect(jsonPath("$.status").value("want_to_watch"))
        .andExpect(jsonPath("$.title").exists());
  }

  @Test
  void testAddToWatchlist_UpdateExisting() throws Exception {
    // Arrange
    Watchlist existingWatchlist = new Watchlist();
    existingWatchlist.setUserId(testUser.getId());
    existingWatchlist.setTitleId(testTitle.getId());
    existingWatchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
    watchlistRepository.save(existingWatchlist);

    WatchlistRequest request = new WatchlistRequest();
    request.setTitleId(testTitle.getId());
    request.setStatus(Watchlist.WatchlistStatus.WATCHING);

    // Act & Assert
    mockMvc.perform(post("/api/v1/watchlist")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("watching"));
  }

  @Test
  void testAddToWatchlist_TitleNotFound() throws Exception {
    // Arrange
    WatchlistRequest request = new WatchlistRequest();
    request.setTitleId(UUID.randomUUID());
    request.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    // Act & Assert
    mockMvc.perform(post("/api/v1/watchlist")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void testAddToWatchlist_InvalidRequest() throws Exception {
    // Arrange - Missing titleId
    WatchlistRequest request = new WatchlistRequest();
    request.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    // Act & Assert
    mockMvc.perform(post("/api/v1/watchlist")
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateWatchlistStatus_Success() throws Exception {
    // Arrange
    Watchlist watchlist = new Watchlist();
    watchlist.setUserId(testUser.getId());
    watchlist.setTitleId(testTitle.getId());
    watchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
    watchlistRepository.save(watchlist);

    WatchlistUpdateRequest request = new WatchlistUpdateRequest();
    request.setStatus(Watchlist.WatchlistStatus.COMPLETED);

    // Act & Assert
    mockMvc.perform(patch("/api/v1/watchlist/{titleId}", testTitle.getId())
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("completed"));
  }

  @Test
  void testUpdateWatchlistStatus_WatchlistItemNotFound() throws Exception {
    // Arrange
    WatchlistUpdateRequest request = new WatchlistUpdateRequest();
    request.setStatus(Watchlist.WatchlistStatus.COMPLETED);

    UUID nonExistentTitleId = UUID.randomUUID();

    // Act & Assert
    mockMvc.perform(patch("/api/v1/watchlist/{titleId}", nonExistentTitleId)
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateWatchlistStatus_InvalidRequest() throws Exception {
    // Arrange
    Watchlist watchlist = new Watchlist();
    watchlist.setUserId(testUser.getId());
    watchlist.setTitleId(testTitle.getId());
    watchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
    watchlistRepository.save(watchlist);

    WatchlistUpdateRequest request = new WatchlistUpdateRequest();
    // Missing status

    // Act & Assert
    mockMvc.perform(patch("/api/v1/watchlist/{titleId}", testTitle.getId())
        .header("Authorization", "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testRemoveFromWatchlist_Success() throws Exception {
    // Arrange
    Watchlist watchlist = new Watchlist();
    watchlist.setUserId(testUser.getId());
    watchlist.setTitleId(testTitle.getId());
    watchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
    watchlistRepository.save(watchlist);

    // Act & Assert
    mockMvc.perform(delete("/api/v1/watchlist/{titleId}", testTitle.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    // Verify deletion
    assertTrue(watchlistRepository.findByUserIdAndTitleId(testUser.getId(), testTitle.getId()).isEmpty());
  }

  @Test
  void testRemoveFromWatchlist_WatchlistItemNotFound() throws Exception {
    // Arrange
    UUID nonExistentTitleId = UUID.randomUUID();

    // Act & Assert
    mockMvc.perform(delete("/api/v1/watchlist/{titleId}", nonExistentTitleId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetWatchlistItem_Success() throws Exception {
    // Arrange
    Watchlist watchlist = new Watchlist();
    watchlist.setUserId(testUser.getId());
    watchlist.setTitleId(testTitle.getId());
    watchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
    watchlistRepository.save(watchlist);

    // Act & Assert
    mockMvc.perform(get("/api/v1/watchlist/{titleId}", testTitle.getId())
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user_id").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.title_id").value(testTitle.getId().toString()))
        .andExpect(jsonPath("$.status").value("want_to_watch"))
        .andExpect(jsonPath("$.title").exists());
  }

  @Test
  void testGetWatchlistItem_NotFound() throws Exception {
    // Arrange
    UUID nonExistentTitleId = UUID.randomUUID();

    // Act & Assert
    mockMvc.perform(get("/api/v1/watchlist/{titleId}", nonExistentTitleId)
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetUserWatchlist_Unauthorized() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/v1/watchlist"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testAddToWatchlist_Unauthorized() throws Exception {
    // Arrange
    WatchlistRequest request = new WatchlistRequest();
    request.setTitleId(testTitle.getId());
    request.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    // Act & Assert
    mockMvc.perform(post("/api/v1/watchlist")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testGetUserWatchlist_WithPagination() throws Exception {
    // Arrange - Create multiple watchlist items
    for (int i = 0; i < 5; i++) {
      Title title = new Title();
      title.setTitle("Test Movie " + i);
      title.setTmdbId(20000 + i); // Use different range to avoid conflict with setUp title (12345)
      title.setType(Title.TitleType.movie);
      title.setSlug("test-movie-pag-" + i);
      title = titleRepository.save(title);

      Watchlist watchlist = new Watchlist();
      watchlist.setUserId(testUser.getId());
      watchlist.setTitleId(title.getId());
      watchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
      watchlistRepository.save(watchlist);
    }

    // Act & Assert
    mockMvc.perform(get("/api/v1/watchlist")
        .param("page", "0")
        .param("size", "2")
        .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(5));
  }
}

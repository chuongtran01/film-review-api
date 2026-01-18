package com.filmreview.util;

import com.filmreview.entity.User;
import com.filmreview.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * Utility class for creating test data in integration tests.
 * Provides helper methods to create users, titles, and other entities
 * and save them to the database, reducing boilerplate in tests.
 */
public class TestDataUtil {

  private final JdbcTemplate jdbcTemplate;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public TestDataUtil(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, UserRepository userRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
  }

  /**
   * Create and save a test user with default password.
   * 
   * @param email    User email
   * @param username Username
   * @return Saved User entity
   */
  public User createAndSaveUser(String email, String username) {
    User user = new User();
    user.setEmail(email);
    user.setUsername(username);
    user.setPasswordHash(passwordEncoder.encode("password123"));
    return userRepository.save(user);
  }

  /**
   * Create and save a test user with email (username auto-generated from email).
   * 
   * @param email User email (username will be extracted from email)
   * @return Saved User entity
   */
  public User createAndSaveUser(String email) {
    String username = email.split("@")[0];
    return createAndSaveUser(email, username);
  }

  /**
   * Create a test title in the database.
   * 
   * @param titleId UUID for the title
   * @param tmdbId  TMDB ID
   * @param title   Title name
   * @param slug    URL slug
   * @return The created title ID
   */
  public UUID createTitle(UUID titleId, Integer tmdbId, String title, String slug) {
    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId, tmdbId, title, slug);
    return titleId;
  }

  /**
   * Create a test title with auto-generated UUID.
   * 
   * @param tmdbId TMDB ID
   * @param title  Title name
   * @param slug   URL slug
   * @return The created title ID
   */
  public UUID createTitle(Integer tmdbId, String title, String slug) {
    UUID titleId = UUID.randomUUID();
    return createTitle(titleId, tmdbId, title, slug);
  }

  /**
   * Clean up test data (users, ratings, titles).
   * Call this in @BeforeEach or @AfterEach.
   */
  public void cleanup() {
    jdbcTemplate.update("DELETE FROM ratings");
    jdbcTemplate.update("DELETE FROM user_roles");
    jdbcTemplate.update("DELETE FROM users");
    jdbcTemplate.update("DELETE FROM titles");
  }
}

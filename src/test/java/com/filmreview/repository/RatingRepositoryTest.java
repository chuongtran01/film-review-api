package com.filmreview.repository;

import com.filmreview.entity.Rating;
import com.filmreview.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
@Transactional
class RatingRepositoryTest {

  @Autowired
  private RatingRepository ratingRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private User testUser;
  private UUID titleId1;
  private UUID titleId2;
  private Rating rating1;
  private Rating rating2;

  @BeforeEach
  void setUp() {
    ratingRepository.deleteAll();
    userRepository.deleteAll();
    jdbcTemplate.update("DELETE FROM titles");

    // Create test user
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setUsername("testuser");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);

    // Create test titles
    titleId1 = UUID.randomUUID();
    titleId2 = UUID.randomUUID();

    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId1, 1, "Test Movie 1", "test-movie-1");
    jdbcTemplate.update(
        "INSERT INTO titles (id, type, tmdb_id, title, slug) VALUES (?, 'movie', ?, ?, ?)",
        titleId2, 2, "Test Movie 2", "test-movie-2");

    // Create test ratings
    rating1 = new Rating();
    rating1.setUserId(testUser.getId());
    rating1.setTitleId(titleId1);
    rating1.setScore(8);
    rating1 = ratingRepository.save(rating1);

    rating2 = new Rating();
    rating2.setUserId(testUser.getId());
    rating2.setTitleId(titleId2);
    rating2.setScore(9);
    rating2 = ratingRepository.save(rating2);
  }

  @Test
  void testFindByUserIdAndTitleId_Success() {
    Optional<Rating> found = ratingRepository.findByUserIdAndTitleId(testUser.getId(), titleId1);

    assertTrue(found.isPresent());
    assertEquals(8, found.get().getScore());
    assertEquals(testUser.getId(), found.get().getUserId());
    assertEquals(titleId1, found.get().getTitleId());
  }

  @Test
  void testFindByUserIdAndTitleId_NotFound() {
    Optional<Rating> found = ratingRepository.findByUserIdAndTitleId(testUser.getId(), UUID.randomUUID());

    assertFalse(found.isPresent());
  }

  @Test
  void testFindByUserIdOrderByCreatedAtDesc() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Rating> ratings = ratingRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId(), pageable);

    assertEquals(2, ratings.getTotalElements());
    assertEquals(2, ratings.getContent().size());
    // Should be ordered by created_at DESC (most recent first)
    assertTrue(ratings.getContent().get(0).getCreatedAt()
        .isAfter(ratings.getContent().get(1).getCreatedAt()) ||
        ratings.getContent().get(0).getCreatedAt()
            .isEqual(ratings.getContent().get(1).getCreatedAt()));
  }

  @Test
  void testFindByTitleIdOrderByCreatedAtDesc() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Rating> ratings = ratingRepository.findByTitleIdOrderByCreatedAtDesc(titleId1, pageable);

    assertEquals(1, ratings.getTotalElements());
    assertEquals(titleId1, ratings.getContent().get(0).getTitleId());
  }

  @Test
  void testExistsByUserIdAndTitleId_True() {
    assertTrue(ratingRepository.existsByUserIdAndTitleId(testUser.getId(), titleId1));
  }

  @Test
  void testExistsByUserIdAndTitleId_False() {
    assertFalse(ratingRepository.existsByUserIdAndTitleId(testUser.getId(), UUID.randomUUID()));
  }

  @Test
  void testCountByTitleId() {
    long count = ratingRepository.countByTitleId(titleId1);
    assertEquals(1, count);

    long count2 = ratingRepository.countByTitleId(titleId2);
    assertEquals(1, count2);

    long count3 = ratingRepository.countByTitleId(UUID.randomUUID());
    assertEquals(0, count3);
  }

  @Test
  void testGetAverageRatingByTitleId() {
    // Create another user for the second rating
    User user2 = new User();
    user2.setEmail("user2@example.com");
    user2.setUsername("user2");
    user2.setPasswordHash(passwordEncoder.encode("password123"));
    user2 = userRepository.save(user2);

    // Add another rating for titleId1
    Rating rating3 = new Rating();
    rating3.setUserId(user2.getId()); // Different user
    rating3.setTitleId(titleId1);
    rating3.setScore(10);
    ratingRepository.save(rating3);

    Double average = ratingRepository.getAverageRatingByTitleId(titleId1);
    assertNotNull(average);
    assertEquals(9.0, average, 0.01); // (8 + 10) / 2 = 9
  }

  @Test
  void testGetAverageRatingByTitleId_NoRatings() {
    Double average = ratingRepository.getAverageRatingByTitleId(UUID.randomUUID());
    assertNull(average);
  }

  @Test
  void testUniqueConstraint_UserTitle() {
    // Try to create duplicate rating
    Rating duplicate = new Rating();
    duplicate.setUserId(testUser.getId());
    duplicate.setTitleId(titleId1); // Same user and title
    duplicate.setScore(5);

    assertThrows(Exception.class, () -> ratingRepository.saveAndFlush(duplicate));
  }
}

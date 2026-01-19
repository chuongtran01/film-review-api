package com.filmreview.faker;

import com.filmreview.entity.Rating;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for generating fake Rating entities for testing.
 * Provides static methods to create Rating instances with random or specified
 * values.
 */
public class RatingFaker {

  private static final SecureRandom secureRandom = new SecureRandom();

  /**
   * Generate a Rating with all random values.
   *
   * @return Rating instance with random values
   */
  public static Rating generate() {
    return generate(null, null, null, null, null, null);
  }

  /**
   * Generate a Rating with specified userId and titleId, random other values.
   *
   * @param userId  User ID
   * @param titleId Title ID
   * @return Rating instance
   */
  public static Rating generate(UUID userId, UUID titleId) {
    return generate(null, userId, titleId, null, null, null);
  }

  /**
   * Generate a Rating with specified userId, titleId, and score, random other
   * values.
   *
   * @param userId  User ID
   * @param titleId Title ID
   * @param score   Rating score (1-10)
   * @return Rating instance
   */
  public static Rating generate(UUID userId, UUID titleId, Integer score) {
    return generate(null, userId, titleId, score, null, null);
  }

  /**
   * Generate a Rating with specified id, userId, titleId, and score, random other
   * values.
   *
   * @param id      Rating ID
   * @param userId  User ID
   * @param titleId Title ID
   * @param score   Rating score (1-10)
   * @return Rating instance
   */
  public static Rating generate(UUID id, UUID userId, UUID titleId, Integer score) {
    return generate(id, userId, titleId, score, null, null);
  }

  /**
   * Generate a Rating with specified values. Null parameters will be randomly
   * generated.
   *
   * @param id        Rating ID (null for random UUID)
   * @param userId    User ID (null for random UUID)
   * @param titleId   Title ID (null for random UUID)
   * @param score     Rating score 1-10 (null for random 1-10)
   * @param createdAt Created timestamp (null for now)
   * @param updatedAt Updated timestamp (null for now)
   * @return Rating instance
   */
  public static Rating generate(
      UUID id,
      UUID userId,
      UUID titleId,
      Integer score,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {

    Rating rating = new Rating();

    rating.setId(id != null ? id : UUID.randomUUID());
    rating.setUserId(userId != null ? userId : UUID.randomUUID());
    rating.setTitleId(titleId != null ? titleId : UUID.randomUUID());
    rating.setScore(score != null ? score : generateRandomScore());
    rating.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
    rating.setUpdatedAt(updatedAt != null ? updatedAt : LocalDateTime.now());

    return rating;
  }

  /**
   * Generate a random score between 1 and 10.
   *
   * @return Random score (1-10)
   */
  private static Integer generateRandomScore() {
    return secureRandom.nextInt(1, 11); // 1 to 10 inclusive
  }
}

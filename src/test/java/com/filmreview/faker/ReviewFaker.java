package com.filmreview.faker;

import com.filmreview.entity.Review;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for generating fake Review entities for testing.
 * Provides static methods to create Review instances with random or specified
 * values.
 */
public class ReviewFaker {

  private static final SecureRandom secureRandom = new SecureRandom();

  private static final String[] REVIEW_TEMPLATES = {
      "This movie was absolutely fantastic! The acting was superb and the storyline kept me engaged throughout.",
      "A solid film with great cinematography. Some parts dragged a bit, but overall enjoyable.",
      "One of the best movies I've seen this year. Highly recommend!",
      "The plot was interesting but the execution fell short. Decent watch but nothing special.",
      "Amazing performances from all actors. The director did a great job bringing this story to life.",
      "I had high expectations but was left disappointed. The pacing was off and characters were underdeveloped.",
      "A masterpiece! Every scene was carefully crafted and the emotional depth was incredible.",
      "Good movie overall, though it had some pacing issues in the middle act.",
      "Brilliant storytelling with unexpected twists. This one will stay with me for a while.",
      "Not my cup of tea. Found it boring and predictable, but I can see why others might enjoy it."
  };

  private static final String[] REVIEW_TITLES = {
      "A Masterpiece of Modern Cinema",
      "Solid Entertainment with Great Performances",
      "One of the Best Films This Year",
      "Interesting Concept, Mixed Execution",
      "Outstanding Direction and Acting",
      "Disappointing Despite High Expectations",
      "Emotionally Powerful and Well-Crafted",
      "Good Film with Minor Pacing Issues",
      "Brilliant Storytelling with Surprising Twists",
      "Not My Favorite, But Decent"
  };

  /**
   * Generate a Review with all random values.
   *
   * @return Review instance with random values
   */
  public static Review generate() {
    return generate(null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Generate a Review with specified userId and titleId, random other values.
   *
   * @param userId  User ID
   * @param titleId Title ID
   * @return Review instance
   */
  public static Review generate(UUID userId, UUID titleId) {
    return generate(null, userId, titleId, null, null, null, null, null, null, null, null);
  }

  /**
   * Generate a Review with specified userId, titleId, and content, random other
   * values.
   *
   * @param userId  User ID
   * @param titleId Title ID
   * @param content Review content
   * @return Review instance
   */
  public static Review generate(UUID userId, UUID titleId, String content) {
    return generate(null, userId, titleId, null, null, content, null, null, null, null, null);
  }

  /**
   * Generate a Review with specified values. Null parameters will be randomly
   * generated.
   *
   * @param id               Review ID (null for random UUID)
   * @param userId           User ID (null for random UUID)
   * @param titleId          Title ID (null for random UUID)
   * @param ratingId         Rating ID (null for random UUID or null)
   * @param title            Review title/headline (null for random)
   * @param content          Review content (null for random)
   * @param containsSpoilers Whether review contains spoilers (null defaults to
   *                         false)
   * @param helpfulCount     Helpful count (null defaults to 0)
   * @param deletedAt        Deleted timestamp (null for not deleted)
   * @param createdAt        Created timestamp (null for now)
   * @param updatedAt        Updated timestamp (null for now)
   * @return Review instance
   */
  public static Review generate(
      UUID id,
      UUID userId,
      UUID titleId,
      UUID ratingId,
      String title,
      String content,
      Boolean containsSpoilers,
      Integer helpfulCount,
      LocalDateTime deletedAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {

    Review review = new Review();

    review.setId(id != null ? id : UUID.randomUUID());
    review.setUserId(userId != null ? userId : UUID.randomUUID());
    review.setTitleId(titleId != null ? titleId : UUID.randomUUID());
    review.setRatingId(
        ratingId != null ? ratingId : (secureRandom.nextBoolean() ? UUID.randomUUID() : null));
    review.setTitle(title != null ? title : generateRandomTitle());
    review.setContent(content != null ? content : generateRandomContent());
    review.setContainsSpoilers(containsSpoilers != null ? containsSpoilers : false);
    review.setHelpfulCount(helpfulCount != null ? helpfulCount : 0);
    review.setDeletedAt(deletedAt);
    review.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
    review.setUpdatedAt(updatedAt != null ? updatedAt : LocalDateTime.now());

    return review;
  }

  /**
   * Generate random review title.
   *
   * @return Random review title
   */
  private static String generateRandomTitle() {
    return REVIEW_TITLES[secureRandom.nextInt(0, REVIEW_TITLES.length)];
  }

  /**
   * Generate random review content.
   *
   * @return Random review text
   */
  private static String generateRandomContent() {
    return REVIEW_TEMPLATES[secureRandom.nextInt(0, REVIEW_TEMPLATES.length)];
  }
}

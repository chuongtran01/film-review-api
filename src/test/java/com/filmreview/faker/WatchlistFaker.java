package com.filmreview.faker;

import com.filmreview.entity.Watchlist;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for generating fake Watchlist entities for testing.
 * Provides static methods to create Watchlist instances with random or
 * specified values.
 */
public class WatchlistFaker {

  private static final SecureRandom secureRandom = new SecureRandom();

  /**
   * Generate a Watchlist with all random values.
   *
   * @return Watchlist instance with random values
   */
  public static Watchlist generate() {
    return generate(null, null, null, null, null, null);
  }

  /**
   * Generate a Watchlist with specified userId and titleId, random other values.
   *
   * @param userId  User ID
   * @param titleId Title ID
   * @return Watchlist instance
   */
  public static Watchlist generate(UUID userId, UUID titleId) {
    return generate(null, userId, titleId, null, null, null);
  }

  /**
   * Generate a Watchlist with specified userId, titleId, and status, random other
   * values.
   *
   * @param userId  User ID
   * @param titleId Title ID
   * @param status  Watchlist status
   * @return Watchlist instance
   */
  public static Watchlist generate(UUID userId, UUID titleId, Watchlist.WatchlistStatus status) {
    return generate(null, userId, titleId, status, null, null);
  }

  /**
   * Generate a Watchlist with specified values. Null parameters will be randomly
   * generated.
   *
   * @param id        Watchlist ID (null for random UUID)
   * @param userId    User ID (null for random UUID)
   * @param titleId   Title ID (null for random UUID)
   * @param status    Watchlist status (null for random)
   * @param createdAt Created timestamp (null for now)
   * @param updatedAt Updated timestamp (null for now)
   * @return Watchlist instance
   */
  public static Watchlist generate(
      UUID id,
      UUID userId,
      UUID titleId,
      Watchlist.WatchlistStatus status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {

    Watchlist watchlist = new Watchlist();

    watchlist.setId(id != null ? id : UUID.randomUUID());
    watchlist.setUserId(userId != null ? userId : UUID.randomUUID());
    watchlist.setTitleId(titleId != null ? titleId : UUID.randomUUID());
    watchlist.setStatus(status != null ? status : generateRandomStatus());
    watchlist.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
    watchlist.setUpdatedAt(updatedAt != null ? updatedAt : LocalDateTime.now());

    return watchlist;
  }

  /**
   * Generate a random watchlist status.
   *
   * @return Random WatchlistStatus
   */
  private static Watchlist.WatchlistStatus generateRandomStatus() {
    Watchlist.WatchlistStatus[] statuses = Watchlist.WatchlistStatus.values();
    return statuses[secureRandom.nextInt(0, statuses.length)];
  }
}

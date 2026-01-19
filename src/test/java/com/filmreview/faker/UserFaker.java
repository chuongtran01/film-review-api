package com.filmreview.faker;

import com.filmreview.entity.User;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for generating fake User entities for testing.
 * Provides static methods to create User instances with random or specified
 * values.
 */
public class UserFaker {

  private static final SecureRandom secureRandom = new SecureRandom();

  /**
   * Generate a User with all random values.
   *
   * @return User instance with random values
   */
  public static User generate() {
    return generate(null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Generate a User with specified email and random other values.
   *
   * @param email User email
   * @return User instance
   */
  public static User generate(String email) {
    return generate(null, email, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Generate a User with specified email and username, random other values.
   *
   * @param email    User email
   * @param username Username
   * @return User instance
   */
  public static User generate(String email, String username) {
    return generate(null, email, username, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Generate a User with specified userId, email, username, and displayName.
   *
   * @param userId      User ID
   * @param email       User email
   * @param username    User username
   * @param displayName User display name
   * @return User instance
   */
  public static User generate(UUID userId, String email, String username, String displayName) {
    return generate(userId, email, username, null, displayName, null, null, null, null, null, null, null, null);
  }

  /**
   * Generate a User with specified userId, email, username, passwordHash, and
   * displayName.
   *
   * @param userId       User ID
   * @param email        User email
   * @param username     User username
   * @param passwordHash User password hash
   * @param displayName  User display name
   * @return User instance
   */
  public static User generate(UUID userId, String email, String username, String passwordHash, String displayName) {
    return generate(userId, email, username, passwordHash, displayName, null, null, null, null, null, null, null, null);
  }

  /**
   * Generate a User with specified values. Null parameters will be randomly
   * generated.
   *
   * @param id              User ID (null for random UUID)
   * @param email           Email address (null for random)
   * @param username        Username (null for random)
   * @param passwordHash    Password hash (null for random)
   * @param displayName     Display name (null for random)
   * @param avatarUrl       Avatar URL (null for random or null)
   * @param bio             Bio text (null for random or null)
   * @param verified        Verified status (null defaults to false)
   * @param oauthProvider   OAuth provider (null for none)
   * @param oauthProviderId OAuth provider ID (null for none)
   * @param oauthEmail      OAuth email (null for none)
   * @param lastActiveAt    Last active timestamp (null for random or null)
   * @param createdAt       Created timestamp (null for now)
   * @return User instance
   */
  public static User generate(
      UUID id,
      String email,
      String username,
      String passwordHash,
      String displayName,
      String avatarUrl,
      String bio,
      Boolean verified,
      String oauthProvider,
      String oauthProviderId,
      String oauthEmail,
      LocalDateTime lastActiveAt,
      LocalDateTime createdAt) {

    User user = new User();

    user.setId(id != null ? id : UUID.randomUUID());
    user.setEmail(email != null ? email : generateRandomEmail());
    user.setUsername(username != null ? username : generateRandomUsername());
    user.setPasswordHash(
        passwordHash != null ? passwordHash
            : "hashed_password_" + RandomStringUtils.random(32, 0, 0, true, true, null, secureRandom));
    user.setDisplayName(displayName != null ? displayName : generateRandomDisplayName());
    user.setAvatarUrl(
        avatarUrl != null ? avatarUrl : (secureRandom.nextBoolean() ? generateRandomAvatarUrl() : null));
    user.setBio(bio != null ? bio : (secureRandom.nextBoolean() ? generateRandomBio() : null));
    user.setVerified(verified != null ? verified : false);
    user.setOauthProvider(oauthProvider);
    user.setOauthProviderId(oauthProviderId);
    user.setOauthEmail(oauthEmail);
    user.setLastActiveAt(lastActiveAt != null ? lastActiveAt
        : (secureRandom.nextBoolean()
            ? LocalDateTime.now().minusDays(secureRandom.nextLong(0, 30))
            : null));
    user.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());

    return user;
  }

  private static String generateRandomEmail() {
    String username = RandomStringUtils.random(8, 0, 0, true, false, null, secureRandom).toLowerCase();
    String domain = "example" + secureRandom.nextInt(1, 10000) + ".com";
    return username + "@" + domain;
  }

  private static String generateRandomUsername() {
    String prefix = RandomStringUtils.random(secureRandom.nextInt(5, 11), 0, 0, true, false, null, secureRandom)
        .toLowerCase();
    int number = secureRandom.nextInt(100, 9999);
    return prefix + number;
  }

  private static String generateRandomDisplayName() {
    String firstName = RandomStringUtils.random(secureRandom.nextInt(5, 11), 0, 0, true, false, null, secureRandom);
    String lastName = RandomStringUtils.random(secureRandom.nextInt(5, 11), 0, 0, true, false, null, secureRandom);
    return firstName + " " + lastName;
  }

  private static String generateRandomAvatarUrl() {
    return "https://example.com/avatars/" + RandomStringUtils.random(16, 0, 0, true, true, null, secureRandom) + ".jpg";
  }

  private static String generateRandomBio() {
    String[] bios = {
        "Movie enthusiast and film critic",
        "Love watching films in my spare time",
        "Passionate about cinema",
        "Film buff since childhood",
        "Reviewing movies one at a time",
        "Cinema lover and critic"
    };
    return bios[secureRandom.nextInt(0, bios.length)];
  }
}

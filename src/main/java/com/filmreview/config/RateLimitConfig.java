package com.filmreview.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate limit configuration using Redis for distributed rate limiting.
 * This allows rate limits to work across multiple application instances.
 */
@Configuration
public class RateLimitConfig {

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  /**
   * Creates a Redis-based proxy manager for distributed rate limiting.
   * Buckets are stored in Redis with automatic expiration.
   */
  @Bean
  public LettuceBasedProxyManager<byte[]> proxyManager() {
    RedisURI redisUri = RedisURI.builder()
        .withHost(redisHost)
        .withPort(redisPort)
        .withPassword(redisPassword != null && !redisPassword.isEmpty() ? redisPassword.toCharArray() : null)
        .build();

    RedisClient redisClient = RedisClient.create(redisUri);
    return LettuceBasedProxyManager.builderFor(redisClient)
        .build();
  }

  /**
   * Anonymous users: 100 requests per hour.
   * Stored in Redis with key prefix "rl:anon:{ip}"
   */
  @Bean
  public Bandwidth anonymousBandwidth() {
    return Bandwidth.builder()
        .capacity(100)
        .refillIntervally(100, Duration.ofHours(1))
        .build();
  }

  /**
   * Authenticated users: 1000 requests per hour.
   * Stored in Redis with key prefix "rl:auth:{userId}"
   */
  @Bean
  public Bandwidth authenticatedBandwidth() {
    return Bandwidth.builder()
        .capacity(1000)
        .refillIntervally(1000, Duration.ofHours(1))
        .build();
  }

  /**
   * Write operations: 50 requests per hour per user.
   * Stored in Redis with key prefix "rl:write:{userId}"
   */
  @Bean
  public Bandwidth writeOperationBandwidth() {
    return Bandwidth.builder()
        .capacity(50)
        .refillIntervally(50, Duration.ofHours(1))
        .build();
  }

  /**
   * Review creation: 10 requests per hour per user.
   * Stored in Redis with key prefix "rl:review:{userId}"
   */
  @Bean
  public Bandwidth reviewCreationBandwidth() {
    return Bandwidth.builder()
        .capacity(10)
        .refillIntervally(10, Duration.ofHours(1))
        .build();
  }

  /**
   * Login attempts: 5 requests per 15 minutes per IP.
   * Stored in Redis with key prefix "rl:login:{ip}"
   */
  @Bean
  public Bandwidth loginAttemptBandwidth() {
    return Bandwidth.builder()
        .capacity(5)
        .refillIntervally(5, Duration.ofMinutes(15))
        .build();
  }
}

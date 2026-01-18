package com.filmreview.security;

import com.filmreview.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting filter using Redis for distributed rate limiting.
 * Applies different rate limits based on:
 * - User authentication status (anonymous vs authenticated)
 * - Endpoint type (write operations, review creation, login attempts)
 * - IP address (for anonymous users and login attempts)
 * 
 * All buckets are stored in Redis, allowing rate limits to work across
 * multiple application instances.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private final LettuceBasedProxyManager<byte[]> proxyManager;
  private final Bandwidth anonymousBandwidth;
  private final Bandwidth authenticatedBandwidth;
  private final Bandwidth writeOperationBandwidth;
  private final Bandwidth reviewCreationBandwidth;
  private final Bandwidth loginAttemptBandwidth;

  public RateLimitFilter(
      LettuceBasedProxyManager<byte[]> proxyManager,
      Bandwidth anonymousBandwidth,
      Bandwidth authenticatedBandwidth,
      Bandwidth writeOperationBandwidth,
      Bandwidth reviewCreationBandwidth,
      Bandwidth loginAttemptBandwidth) {
    this.proxyManager = proxyManager;
    this.anonymousBandwidth = anonymousBandwidth;
    this.authenticatedBandwidth = authenticatedBandwidth;
    this.writeOperationBandwidth = writeOperationBandwidth;
    this.reviewCreationBandwidth = reviewCreationBandwidth;
    this.loginAttemptBandwidth = loginAttemptBandwidth;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();
    String method = request.getMethod();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
        && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);

    // Skip rate limiting for health check endpoint
    if (path.equals("/api/v1/health")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Note: retryAfter is calculated even when requests are allowed to help clients
      // know when the bucket will be fully refilled, enabling proactive request
      // pacing.

      // Check login attempt rate limit (by IP)
      if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register")) {
        String clientIp = getClientIpAddress(request);
        String keyPrefix = "rl:login:" + clientIp;
        checkRateLimit(keyPrefix, loginAttemptBandwidth, 5,
            "Too many login attempts. Please try again later.", response);
        filterChain.doFilter(request, response);
        return;
      }

      // Check review creation rate limit (per user)
      if (path.startsWith("/api/v1/reviews") && method.equals("POST")) {
        if (!isAuthenticated) {
          throw new RateLimitExceededException("Authentication required", 0);
        }
        String userId = getUserId(authentication);
        String keyPrefix = "rl:review:" + userId;
        checkRateLimit(keyPrefix, reviewCreationBandwidth, 10,
            "Review creation rate limit exceeded. Maximum 10 reviews per hour.", response);
        filterChain.doFilter(request, response);
        return;
      }

      // Check write operation rate limit (per user)
      if (isWriteOperation(method, path) && isAuthenticated) {
        String userId = getUserId(authentication);
        String keyPrefix = "rl:write:" + userId;
        checkRateLimit(keyPrefix, writeOperationBandwidth, 50,
            "Write operation rate limit exceeded. Maximum 50 write operations per hour.", response);
        filterChain.doFilter(request, response);
        return;
      }

      // Check general rate limit (anonymous or authenticated)
      String keyPrefix;
      Bandwidth bandwidth;
      int limit;
      if (isAuthenticated) {
        String userId = getUserId(authentication);
        keyPrefix = "rl:auth:" + userId;
        bandwidth = authenticatedBandwidth;
        limit = 1000;
      } else {
        String clientIp = getClientIpAddress(request);
        keyPrefix = "rl:anon:" + clientIp;
        bandwidth = anonymousBandwidth;
        limit = 100;
      }

      checkRateLimit(keyPrefix, bandwidth, limit,
          String.format("Rate limit exceeded. Maximum %d requests per hour.", limit), response);
      filterChain.doFilter(request, response);

    } catch (RateLimitExceededException e) {
      response.setStatus(429); // HTTP 429 Too Many Requests
      response.setHeader("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
      response.setContentType("application/json");
      response.getWriter().write(String.format(
          "{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"%s\",\"retryAfter\":%d}",
          e.getMessage(), e.getRetryAfterSeconds()));
    }
  }

  private boolean isWriteOperation(String method, String path) {
    return method.equals("POST") || method.equals("PUT") || method.equals("PATCH")
        || method.equals("DELETE");
  }

  private String getUserId(Authentication authentication) {
    if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
      return userPrincipal.getId().toString();
    }
    return authentication.getName();
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }

  /**
   * Checks rate limit for a given key prefix and bandwidth configuration.
   * 
   * @param keyPrefix    Redis key prefix for the rate limit bucket (e.g.,
   *                     "rl:login:192.168.1.1")
   * @param bandwidth    Bandwidth configuration for this rate limit
   * @param limit        Maximum number of requests allowed (for headers)
   * @param errorMessage Error message to throw if rate limit is exceeded
   * @param response     HTTP response to add rate limit headers to
   * @throws RateLimitExceededException if rate limit is exceeded
   */
  private void checkRateLimit(String keyPrefix, Bandwidth bandwidth, int limit,
      String errorMessage, HttpServletResponse response) {
    byte[] key = keyPrefix.getBytes(StandardCharsets.UTF_8);
    BucketConfiguration config = BucketConfiguration.builder()
        .addLimit(bandwidth)
        .build();
    Bucket bucket = proxyManager.builder()
        .build(key, () -> config);
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (!probe.isConsumed()) {
      long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
      throw new RateLimitExceededException(errorMessage, retryAfterSeconds);
    }

    long retryAfter = probe.getNanosToWaitForRefill() / 1_000_000_000;
    addRateLimitHeaders(response, limit, probe.getRemainingTokens(), retryAfter);
  }

  private void addRateLimitHeaders(HttpServletResponse response, long limit, long remaining,
      long retryAfterSeconds) {
    response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
    if (retryAfterSeconds > 0) {
      response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
    }
  }
}

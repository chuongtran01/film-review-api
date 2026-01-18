package com.filmreview.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret:your-secret-key-change-this-in-production-min-256-bits}")
  private String secret;

  @Value("${jwt.access-token-expiration:900000}") // 15 minutes in milliseconds
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token-expiration:604800000}") // 7 days in milliseconds
  private long refreshTokenExpiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(UUID userId, String username, String email) {
    return generateToken(userId, username, email, accessTokenExpiration);
  }

  public String generateRefreshToken(UUID userId, String username, String email) {
    return generateToken(userId, username, email, refreshTokenExpiration);
  }

  private String generateToken(UUID userId, String username, String email, long expiration) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .claim("email", email)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
  }

  public UUID getUserIdFromToken(String token) {
    String subject = getClaimFromToken(token, Claims::getSubject);
    return UUID.fromString(subject);
  }

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, claims -> claims.get("username", String.class));
  }

  public String getEmailFromToken(String token) {
    return getClaimFromToken(token, claims -> claims.get("email", String.class));
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public Boolean validateToken(String token) {
    try {
      getAllClaimsFromToken(token);
      return !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }
}

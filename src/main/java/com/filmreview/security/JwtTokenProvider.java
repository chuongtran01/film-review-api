package com.filmreview.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
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

  public String generateAccessToken(UUID userId, String username, String email, List<String> roles,
      List<String> permissions) {
    return generateToken(userId, username, email, roles, permissions, accessTokenExpiration);
  }

  public String generateRefreshToken(UUID userId, String username, String email, List<String> roles,
      List<String> permissions) {
    return generateToken(userId, username, email, roles, permissions, refreshTokenExpiration);
  }

  private String generateToken(UUID userId, String username, String email, List<String> roles, List<String> permissions,
      long expiration) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .claim("email", email)
        .claim("roles", roles != null ? roles : List.of("USER"))
        .claim("permissions", permissions != null ? permissions : List.of())
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

  @SuppressWarnings("unchecked")
  public List<String> getRolesFromToken(String token) {
    return getClaimFromToken(token, claims -> {
      Object roles = claims.get("roles");
      if (roles instanceof List) {
        return (List<String>) roles;
      }
      return List.of("USER");
    });
  }

  @SuppressWarnings("unchecked")
  public List<String> getPermissionsFromToken(String token) {
    return getClaimFromToken(token, claims -> {
      Object permissions = claims.get("permissions");
      if (permissions instanceof List) {
        return (List<String>) permissions;
      }
      return List.of();
    });
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

package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_created_at", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "username", unique = true, nullable = false, length = 50)
  private String username;

  @Column(name = "email", unique = true, nullable = false, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "display_name", length = 100)
  private String displayName;

  @Column(name = "avatar_url", columnDefinition = "TEXT")
  private String avatarUrl;

  @Column(name = "bio", columnDefinition = "TEXT")
  private String bio;

  @Column(name = "verified", nullable = false)
  private Boolean verified = false;

  // OAuth fields (for V1+)
  @Column(name = "oauth_provider", length = 50)
  private String oauthProvider;

  @Column(name = "oauth_provider_id", length = 255)
  private String oauthProviderId;

  @Column(name = "oauth_email", length = 255)
  private String oauthEmail;

  @Column(name = "last_active_at")
  private LocalDateTime lastActiveAt;
}

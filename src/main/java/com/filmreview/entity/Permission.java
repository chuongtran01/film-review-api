package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Permission entity representing specific permissions in the system.
 * Permissions are assigned to roles, and users get permissions through their
 * roles.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name", unique = true, nullable = false, length = 100)
  private String name; // e.g., "titles.create", "titles.delete", "users.manage"

  @Column(name = "display_name", length = 100)
  private String displayName; // e.g., "Create Titles", "Delete Titles", "Manage Users"

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }
}

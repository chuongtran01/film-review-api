package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Genre entity representing movie/TV show genres.
 * Genres are seeded from TMDB and don't change frequently.
 */
@Entity
@Table(name = "genres", indexes = {
    @Index(name = "idx_genres_slug", columnList = "slug")
})
@Getter
@Setter
@NoArgsConstructor
public class Genre {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name", unique = true, nullable = false, length = 100)
  private String name;

  @Column(name = "slug", unique = true, nullable = false, length = 100)
  private String slug;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}

package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Language entity representing languages used in movies/TV shows.
 * Languages are synced from TMDB and use ISO 639-1 codes as primary keys.
 */
@Entity
@Table(name = "languages", indexes = {
    @Index(name = "idx_languages_iso_639_1", columnList = "iso_639_1")
})
@Getter
@Setter
@NoArgsConstructor
public class Language {

  @Id
  @Column(name = "iso_639_1", length = 10)
  private String iso6391;

  @Column(name = "english_name", nullable = false, length = 255)
  private String englishName;

  @Column(name = "name", length = 255)
  private String name;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}

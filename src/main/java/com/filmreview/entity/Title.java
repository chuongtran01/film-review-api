package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Title entity representing movies.
 * Stores metadata from TMDB and user-generated aggregated data.
 * Note: Database schema includes TV show fields for future use, but currently
 * only movies are supported.
 */
@Entity
@Table(name = "titles", indexes = {
    @Index(name = "idx_titles_tmdb_id", columnList = "tmdb_id"),
    @Index(name = "idx_titles_imdb_id", columnList = "imdb_id"),
    @Index(name = "idx_titles_slug", columnList = "slug"),
    @Index(name = "idx_titles_type", columnList = "type"),
    @Index(name = "idx_titles_release_date", columnList = "release_date DESC"),
    @Index(name = "idx_titles_type_release", columnList = "type, release_date DESC"),
    @Index(name = "idx_titles_rating_avg", columnList = "user_rating_avg DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class Title extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private TitleType type;

  @Column(name = "tmdb_id", unique = true, nullable = false)
  private Integer tmdbId;

  @Column(name = "imdb_id", length = 20)
  private String imdbId;

  @Column(name = "title", nullable = false, length = 500)
  private String title;

  @Column(name = "original_title", length = 500)
  private String originalTitle;

  @Column(name = "slug", unique = true, nullable = false, length = 500)
  private String slug;

  @Column(name = "synopsis", columnDefinition = "TEXT")
  private String synopsis;

  @Column(name = "release_date")
  private LocalDate releaseDate;

  @Column(name = "runtime")
  private Integer runtime; // minutes

  @Column(name = "poster_url", columnDefinition = "TEXT")
  private String posterUrl;

  @Column(name = "backdrop_url", columnDefinition = "TEXT")
  private String backdropUrl;

  @Column(name = "status", length = 50)
  private String status; // released, upcoming, etc.

  // Aggregated scores (cached, updated via triggers)
  @Column(name = "user_rating_avg", precision = 3, scale = 2)
  private java.math.BigDecimal userRatingAvg; // 0.00 to 10.00

  @Column(name = "user_rating_count")
  private Integer userRatingCount = 0;

  // TV show specific (nullable for movies)
  @Column(name = "number_of_seasons")
  private Integer numberOfSeasons;

  @Column(name = "number_of_episodes")
  private Integer numberOfEpisodes;

  @Column(name = "first_air_date")
  private LocalDate firstAirDate;

  public enum TitleType {
    movie,
    tv_show
  }
}

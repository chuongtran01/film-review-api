package com.filmreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Title entity.
 * Used to expose title data via API without exposing internal entity structure.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TitleDto {

  private UUID id;
  private Integer tmdbId;
  private String imdbId;
  private String type; // "movie" or "tv_show"
  private String title;
  private String originalTitle;
  private String slug;
  private String synopsis;
  private LocalDate releaseDate;
  private Integer runtime; // minutes
  private String posterUrl;
  private String backdropUrl;
  private String status;
  private BigDecimal userRatingAvg; // 0.00 to 10.00
  private Integer userRatingCount;

  // TV show specific fields
  private Integer numberOfSeasons;
  private Integer numberOfEpisodes;
  private LocalDate firstAirDate;

  // Genres
  private List<GenreDto> genres;

  // Timestamps from BaseEntity
  private java.time.LocalDateTime createdAt;
  private java.time.LocalDateTime updatedAt;
}

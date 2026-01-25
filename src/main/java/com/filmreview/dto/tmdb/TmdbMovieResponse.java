package com.filmreview.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * TMDB Movie API response DTO.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieResponse {

  private Integer id;
  private String title;

  @JsonProperty("original_title")
  private String originalTitle;

  @JsonProperty("overview")
  private String overview;

  @JsonProperty("release_date")
  private LocalDate releaseDate;

  @JsonProperty("runtime")
  private Integer runtime;

  @JsonProperty("poster_path")
  private String posterPath;

  @JsonProperty("backdrop_path")
  private String backdropPath;

  @JsonProperty("status")
  private String status;

  @JsonProperty("imdb_id")
  private String imdbId;

  @JsonProperty("popularity")
  private Double popularity;

  @JsonProperty("vote_average")
  private Double voteAverage;

  @JsonProperty("vote_count")
  private Integer voteCount;

  @JsonProperty("genres")
  private List<TmdbGenre> genres;

  @JsonProperty("credits")
  private TmdbCredits credits;

  @JsonProperty("budget")
  private Integer budget;

  @JsonProperty("homepage")
  private String homepage;

  @JsonProperty("original_language")
  private String originalLanguage;

  @JsonProperty("production_companies")
  private List<TmdbProductionCompany> productionCompanies;

  @JsonProperty("production_countries")
  private List<TmdbProductionCountry> productionCountries;

  @JsonProperty("revenue")
  private Integer revenue;

  @JsonProperty("spoken_languages")
  private List<TmdbSpokenLanguage> spokenLanguages;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbGenre {
    private Integer id;
    private String name;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbCredits {
    @JsonProperty("cast")
    private List<TmdbCast> cast;

    @JsonProperty("crew")
    private List<TmdbCrew> crew;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbCast {
    private Integer id;
    private String name;
    private String character;
    @JsonProperty("order")
    private Integer orderIndex;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbCrew {
    private Integer id;
    private String name;
    private String job;
    private String department;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbProductionCompany {
    private Integer id;
    @JsonProperty("logo_path")
    private String logoPath;
    private String name;
    @JsonProperty("origin_country")
    private String originCountry;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbProductionCountry {
    @JsonProperty("iso_3166_1")
    private String iso3166_1;
    private String name;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbSpokenLanguage {
    @JsonProperty("english_name")
    private String englishName;
    @JsonProperty("iso_639_1")
    private String iso639_1;
    private String name;
  }
}

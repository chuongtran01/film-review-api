package com.filmreview.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * TMDB TV Series API response DTO.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbTvSeriesResponse {

  private Integer id;
  private String name;

  @JsonProperty("original_name")
  private String originalName;

  @JsonProperty("overview")
  private String overview;

  @JsonProperty("first_air_date")
  private LocalDate firstAirDate;

  @JsonProperty("last_air_date")
  private LocalDate lastAirDate;

  @JsonProperty("number_of_seasons")
  private Integer numberOfSeasons;

  @JsonProperty("number_of_episodes")
  private Integer numberOfEpisodes;

  @JsonProperty("poster_path")
  private String posterPath;

  @JsonProperty("backdrop_path")
  private String backdropPath;

  @JsonProperty("status")
  private String status;

  @JsonProperty("tagline")
  private String tagline;

  @JsonProperty("type")
  private String type;

  @JsonProperty("popularity")
  private Double popularity;

  @JsonProperty("vote_average")
  private Double voteAverage;

  @JsonProperty("vote_count")
  private Integer voteCount;

  @JsonProperty("adult")
  private Boolean adult;

  @JsonProperty("homepage")
  private String homepage;

  @JsonProperty("in_production")
  private Boolean inProduction;

  @JsonProperty("original_language")
  private String originalLanguage;

  @JsonProperty("episode_run_time")
  private List<Integer> episodeRunTime;

  @JsonProperty("languages")
  private List<String> languages;

  @JsonProperty("origin_country")
  private List<String> originCountry;

  @JsonProperty("genres")
  private List<TmdbGenre> genres;

  @JsonProperty("created_by")
  private List<TmdbCreatedBy> createdBy;

  @JsonProperty("last_episode_to_air")
  private TmdbEpisode lastEpisodeToAir;

  @JsonProperty("networks")
  private List<TmdbNetwork> networks;

  @JsonProperty("production_companies")
  private List<TmdbProductionCompany> productionCompanies;

  @JsonProperty("production_countries")
  private List<TmdbProductionCountry> productionCountries;

  @JsonProperty("seasons")
  private List<TmdbSeason> seasons;

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
  public static class TmdbCreatedBy {
    private Integer id;

    @JsonProperty("credit_id")
    private String creditId;

    private String name;

    private Integer gender;

    @JsonProperty("profile_path")
    private String profilePath;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbEpisode {
    private Integer id;
    private String name;
    private String overview;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    @JsonProperty("air_date")
    private LocalDate airDate;

    @JsonProperty("episode_number")
    private Integer episodeNumber;

    @JsonProperty("production_code")
    private String productionCode;

    private Integer runtime;

    @JsonProperty("season_number")
    private Integer seasonNumber;

    @JsonProperty("show_id")
    private Integer showId;

    @JsonProperty("still_path")
    private String stillPath;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbNetwork {
    private Integer id;

    @JsonProperty("logo_path")
    private String logoPath;

    private String name;

    @JsonProperty("origin_country")
    private String originCountry;
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
  public static class TmdbSeason {
    @JsonProperty("air_date")
    private LocalDate airDate;

    @JsonProperty("episode_count")
    private Integer episodeCount;

    private Integer id;
    private String name;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("season_number")
    private Integer seasonNumber;

    @JsonProperty("vote_average")
    private Double voteAverage;
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

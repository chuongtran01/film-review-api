package com.filmreview.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * TMDB paginated response wrapper.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbPageResponse<T> {

  private Integer page;

  @JsonProperty("total_pages")
  private Integer totalPages;

  @JsonProperty("total_results")
  private Integer totalResults;

  private List<T> results;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TmdbMovieItem {
    private Integer id;
    private String title;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("popularity")
    private Double popularity;
  }
}

package com.filmreview.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO for TMDB language information.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbLanguageInfo {

  @JsonProperty("iso_639_1")
  private String iso6391;

  @JsonProperty("english_name")
  private String englishName;

  @JsonProperty("name")
  private String name;

  public TmdbLanguageInfo() {
  }

  public TmdbLanguageInfo(String iso6391, String englishName, String name) {
    this.iso6391 = iso6391;
    this.englishName = englishName;
    this.name = name;
  }
}

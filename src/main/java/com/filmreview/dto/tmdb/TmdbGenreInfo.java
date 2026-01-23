package com.filmreview.dto.tmdb;

/**
 * DTO for TMDB genre information.
 */
public class TmdbGenreInfo {
  private final Integer id;
  private final String name;

  public TmdbGenreInfo(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}

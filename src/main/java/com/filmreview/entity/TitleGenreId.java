package com.filmreview.entity;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite key for TitleGenre entity.
 */
public class TitleGenreId implements Serializable {

  private UUID titleId;
  private Integer genreId;

  public TitleGenreId() {
  }

  public TitleGenreId(UUID titleId, Integer genreId) {
    this.titleId = titleId;
    this.genreId = genreId;
  }

  public UUID getTitleId() {
    return titleId;
  }

  public void setTitleId(UUID titleId) {
    this.titleId = titleId;
  }

  public Integer getGenreId() {
    return genreId;
  }

  public void setGenreId(Integer genreId) {
    this.genreId = genreId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    TitleGenreId that = (TitleGenreId) o;

    if (!titleId.equals(that.titleId))
      return false;
    return genreId.equals(that.genreId);
  }

  @Override
  public int hashCode() {
    int result = titleId.hashCode();
    result = 31 * result + genreId.hashCode();
    return result;
  }
}

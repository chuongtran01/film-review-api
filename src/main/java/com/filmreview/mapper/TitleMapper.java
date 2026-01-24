package com.filmreview.mapper;

import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.entity.Title;
import com.filmreview.service.TmdbService;
import com.filmreview.util.SlugUtils;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting TMDB DTOs to Title entities.
 */
@Component
public class TitleMapper {

  private final TmdbService tmdbService;

  public TitleMapper(TmdbService tmdbService) {
    this.tmdbService = tmdbService;
  }

  /**
   * Map TMDB movie response to Title entity.
   */
  public Title toTitle(TmdbMovieResponse response) {
    Title title = new Title();
    title.setType(Title.TitleType.movie);
    title.setTmdbId(response.getId());
    title.setImdbId(response.getImdbId());
    title.setTitle(response.getTitle());
    title.setOriginalTitle(response.getOriginalTitle());
    title.setSlug(SlugUtils.generateSlug(response.getTitle()));
    title.setSynopsis(response.getOverview());
    title.setReleaseDate(response.getReleaseDate());
    title.setRuntime(response.getRuntime());
    // Only set poster/backdrop URLs if paths are available
    if (response.getPosterPath() != null && !response.getPosterPath().isEmpty()) {
      title.setPosterUrl(tmdbService.getImageUrl(response.getPosterPath(), "w500"));
    }
    if (response.getBackdropPath() != null && !response.getBackdropPath().isEmpty()) {
      title.setBackdropUrl(tmdbService.getImageUrl(response.getBackdropPath(), "w1920"));
    }
    title.setStatus(response.getStatus());
    title.setUserRatingCount(0);
    return title;
  }
}

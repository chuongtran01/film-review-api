package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbGenreInfo;
import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.dto.tmdb.TmdbPageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface for interacting with TMDB API.
 */
public interface TmdbService {

  /**
   * Get movie details by TMDB ID.
   */
  TmdbMovieResponse getMovieDetails(Integer tmdbId);

  /**
   * Get popular movies as Spring Data Page (pageable).
   */
  Page<TmdbPageResponse.TmdbMovieItem> getPopularMovies(String language, int page, String region);

  /**
   * Get popular TV shows as Spring Data Page (pageable).
   */
  Page<TmdbPageResponse.TmdbTvSeriesItem> getPopularTVShows(String language, int page);

  /**
   * Get official movie genres list from TMDB.
   */
  List<TmdbGenreInfo> getMovieGenres();

  /**
   * Get official TV series genres list from TMDB.
   */
  List<TmdbGenreInfo> getTvSeriesGenres();

  /**
   * Get image URL for a given path.
   */
  String getImageUrl(String path, String size);
}

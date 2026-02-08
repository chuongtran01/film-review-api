package com.filmreview.service;

import com.filmreview.entity.Title;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Interface for managing movie titles.
 * Implements on-demand fetching: DB miss → TMDB → store → return.
 */
public interface TitleService {

  /**
   * Get title by ID. If not found, throws NotFoundException.
   */
  Title getTitleById(UUID id);

  /**
   * Get title by slug. If not found, throws NotFoundException.
   */
  Title getTitleBySlug(String slug);

  /**
   * Fetch movie from TMDB and save to database.
   */
  Title fetchAndSaveMovie(Integer tmdbId);

  /**
   * Fetch TV series from TMDB and save to database.
   */
  Title fetchAndSaveTvSeries(Integer tmdbId);

  /**
   * Get title by TMDB ID with type. If not in DB, fetches from TMDB and stores
   * it.
   * 
   * @param tmdbId The TMDB ID
   * @param type   The type: "movie" or "tv_show"
   * @return The title entity
   */
  Title getTitleByTmdbId(Integer tmdbId, String type);

  /**
   * Get popular movies from TMDB. Fetches and saves movies that don't exist in
   * DB.
   */
  Page<Title> getPopularMovies(String language, int page, String region, Pageable pageable);

  /**
   * Get popular TV shows from TMDB. Fetches and saves TV shows that don't exist
   * in
   * DB.
   */
  Page<Title> getPopularTVShows(String language, int page, Pageable pageable);

  /**
   * Search titles by query string.
   * Searches in title and originalTitle fields (case-insensitive).
   * 
   * @param query    The search query
   * @param type     Optional type filter (movie or tv_show)
   * @param pageable Pagination parameters
   * @return Page of matching titles
   */
  Page<Title> searchTitles(String query, String type, Pageable pageable);
}

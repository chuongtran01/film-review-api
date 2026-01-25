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
   * Get movie by TMDB ID. If not in DB, fetches from TMDB and stores it.
   * This is the on-demand fetching logic for MVP.
   */
  Title getTitleByTmdbId(Integer tmdbId);

  /**
   * Fetch movie from TMDB and save to database.
   */
  Title fetchAndSaveTitle(Integer tmdbId);

  /**
   * Get popular movies from TMDB. Fetches and saves movies that don't exist in
   * DB.
   */
  Page<Title> getPopularMovies(String language, int page, String region, Pageable pageable);

  /**
   * Get popular TV shows from TMDB. Fetches and saves TV shows that don't exist in
   * DB.
   */
  Page<Title> getPopularTVShows(String language, int page, Pageable pageable);
}

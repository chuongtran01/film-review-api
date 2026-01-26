package com.filmreview.service;

import com.filmreview.entity.Genre;

import java.util.List;

/**
 * Interface for managing genres.
 * Handles syncing genres from TMDB.
 */
public interface GenreService {

  /**
   * Get all genres from the database.
   * 
   * @return List of all genres
   */
  List<Genre> getAllGenres();

  /**
   * Sync movie genres from TMDB.
   * Fetches official genre list and updates the database.
   * 
   * @return Number of genres synced
   */
  int syncMovieGenres();

  /**
   * Sync TV series genres from TMDB.
   * Fetches official genre list and updates the database.
   * 
   * @return Number of genres synced
   */
  int syncTvSeriesGenres();
}

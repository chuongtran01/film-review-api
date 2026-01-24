package com.filmreview.service;

/**
 * Interface for managing genres.
 * Handles syncing genres from TMDB.
 */
public interface GenreService {

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

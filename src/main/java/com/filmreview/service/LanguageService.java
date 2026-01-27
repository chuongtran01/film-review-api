package com.filmreview.service;

import com.filmreview.entity.Language;

import java.util.List;

/**
 * Interface for managing languages.
 * Handles syncing languages from TMDB.
 */
public interface LanguageService {

  /**
   * Get all languages from the database.
   * 
   * @return List of all languages
   */
  List<Language> getAllLanguages();

  /**
   * Sync languages from TMDB.
   * Fetches official language list and updates the database.
   * 
   * @return Number of languages synced
   */
  int syncLanguages();
}

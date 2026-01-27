package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbLanguageInfo;
import com.filmreview.entity.Language;
import com.filmreview.repository.LanguageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of LanguageService for managing languages.
 * Handles syncing languages from TMDB.
 */
@Service
public class LanguageServiceImpl implements LanguageService {

  private static final Logger logger = LoggerFactory.getLogger(LanguageServiceImpl.class);

  private final LanguageRepository languageRepository;
  private final TmdbService tmdbService;

  public LanguageServiceImpl(LanguageRepository languageRepository, TmdbService tmdbService) {
    this.languageRepository = languageRepository;
    this.tmdbService = tmdbService;
  }

  @Override
  public List<Language> getAllLanguages() {
    return languageRepository.findAll();
  }

  @Override
  @Transactional
  public int syncLanguages() {
    logger.info("Starting languages sync from TMDB");

    List<TmdbLanguageInfo> tmdbLanguages = tmdbService.getLanguages();
    if (tmdbLanguages == null || tmdbLanguages.isEmpty()) {
      logger.warn("No languages returned from TMDB");
      return 0;
    }

    int synced = 0;
    for (TmdbLanguageInfo tmdbLanguage : tmdbLanguages) {
      // Skip if iso_639_1 is null or empty
      if (tmdbLanguage.getIso6391() == null || tmdbLanguage.getIso6391().isEmpty()) {
        logger.debug("Skipping language with null or empty iso_639_1");
        continue;
      }

      Language language = languageRepository.findById(tmdbLanguage.getIso6391())
          .orElseGet(() -> {
            Language newLanguage = new Language();
            newLanguage.setIso6391(tmdbLanguage.getIso6391());
            return newLanguage;
          });

      // Update englishName and name if changed
      boolean updated = false;
      String newEnglishName = tmdbLanguage.getEnglishName() != null ? tmdbLanguage.getEnglishName() : "";
      String currentEnglishName = language.getEnglishName() != null ? language.getEnglishName() : "";
      if (!newEnglishName.equals(currentEnglishName)) {
        language.setEnglishName(tmdbLanguage.getEnglishName());
        updated = true;
      }

      // Handle name field (may be null or empty)
      String newName = tmdbLanguage.getName() != null ? tmdbLanguage.getName() : "";
      String currentName = language.getName() != null ? language.getName() : "";
      if (!newName.equals(currentName)) {
        language.setName(newName.isEmpty() ? null : newName);
        updated = true;
      }

      if (updated || language.getCreatedAt() == null) {
        languageRepository.save(language);
        synced++;
        logger.debug("Synced language: iso_639_1={}, englishName={}",
            language.getIso6391(), language.getEnglishName());
      }
    }

    logger.info("Completed languages sync: {} languages processed", synced);
    return synced;
  }
}

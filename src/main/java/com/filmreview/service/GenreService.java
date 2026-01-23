package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbGenreInfo;
import com.filmreview.entity.Genre;
import com.filmreview.repository.GenreRepository;
import com.filmreview.util.SlugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing genres.
 * Handles syncing genres from TMDB.
 */
@Service
public class GenreService {

  private static final Logger logger = LoggerFactory.getLogger(GenreService.class);

  private final GenreRepository genreRepository;
  private final TmdbService tmdbService;

  public GenreService(GenreRepository genreRepository, TmdbService tmdbService) {
    this.genreRepository = genreRepository;
    this.tmdbService = tmdbService;
  }

  /**
   * Sync movie genres from TMDB.
   * Fetches official genre list and updates the database.
   * 
   * @return Number of genres synced
   */
  @Transactional
  public int syncMovieGenres() {
    logger.info("Starting movie genres sync from TMDB");

    List<TmdbGenreInfo> tmdbGenres = tmdbService.getMovieGenres();
    if (tmdbGenres == null || tmdbGenres.isEmpty()) {
      logger.warn("No genres returned from TMDB");
      return 0;
    }

    int synced = 0;
    for (TmdbGenreInfo tmdbGenre : tmdbGenres) {
      Genre genre = genreRepository.findById(tmdbGenre.getId())
          .orElseGet(() -> {
            Genre newGenre = new Genre();
            newGenre.setId(tmdbGenre.getId());
            return newGenre;
          });

      // Update name and slug if changed
      boolean updated = false;
      if (!tmdbGenre.getName().equals(genre.getName())) {
        genre.setName(tmdbGenre.getName());
        updated = true;
      }

      String slug = SlugUtils.generateSlug(tmdbGenre.getName(), 100);
      if (!slug.equals(genre.getSlug())) {
        genre.setSlug(slug);
        updated = true;
      }

      if (updated || genre.getCreatedAt() == null) {
        genreRepository.save(genre);
        synced++;
        logger.debug("Synced genre: id={}, name={}", genre.getId(), genre.getName());
      }
    }

    logger.info("Completed movie genres sync: {} genres processed", synced);
    return synced;
  }

  /**
   * Sync TV series genres from TMDB.
   * Fetches official genre list and updates the database.
   * 
   * @return Number of genres synced
   */
  @Transactional
  public int syncTvSeriesGenres() {
    logger.info("Starting TV series genres sync from TMDB");

    List<TmdbGenreInfo> tmdbGenres = tmdbService.getTvSeriesGenres();
    if (tmdbGenres == null || tmdbGenres.isEmpty()) {
      logger.warn("No genres returned from TMDB");
      return 0;
    }

    int synced = 0;
    for (TmdbGenreInfo tmdbGenre : tmdbGenres) {
      Genre genre = genreRepository.findById(tmdbGenre.getId())
          .orElseGet(() -> {
            Genre newGenre = new Genre();
            newGenre.setId(tmdbGenre.getId());
            return newGenre;
          });

      // Update name and slug if changed
      boolean updated = false;
      if (!tmdbGenre.getName().equals(genre.getName())) {
        genre.setName(tmdbGenre.getName());
        updated = true;
      }

      String slug = SlugUtils.generateSlug(tmdbGenre.getName(), 100);
      if (!slug.equals(genre.getSlug())) {
        genre.setSlug(slug);
        updated = true;
      }

      if (updated || genre.getCreatedAt() == null) {
        genreRepository.save(genre);
        synced++;
        logger.debug("Synced genre: id={}, name={}", genre.getId(), genre.getName());
      }
    }

    logger.info("Completed TV series genres sync: {} genres processed", synced);
    return synced;
  }
}

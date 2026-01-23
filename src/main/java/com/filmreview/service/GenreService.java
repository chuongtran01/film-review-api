package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbGenreInfo;
import com.filmreview.entity.Genre;
import com.filmreview.repository.GenreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for managing genres.
 * Handles syncing genres from TMDB.
 */
@Service
public class GenreService {

  private static final Logger logger = LoggerFactory.getLogger(GenreService.class);
  private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

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

      String slug = generateSlug(tmdbGenre.getName());
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

      String slug = generateSlug(tmdbGenre.getName());
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

  /**
   * Generate URL-friendly slug from genre name.
   */
  private String generateSlug(String text) {
    if (text == null || text.isEmpty()) {
      return "untitled";
    }

    String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
    String slug = NON_LATIN.matcher(normalized).replaceAll("");
    slug = WHITESPACE.matcher(slug).replaceAll("-");
    slug = slug.toLowerCase();

    return slug.length() > 100 ? slug.substring(0, 100) : slug;
  }
}

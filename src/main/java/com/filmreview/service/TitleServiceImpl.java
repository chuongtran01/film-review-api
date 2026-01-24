package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.entity.Genre;
import com.filmreview.entity.Title;
import com.filmreview.entity.TitleGenre;
import com.filmreview.entity.TitleGenreId;
import com.filmreview.exception.NotFoundException;
import com.filmreview.mapper.TitleMapper;
import com.filmreview.repository.GenreRepository;
import com.filmreview.repository.TitleGenreRepository;
import com.filmreview.repository.TitleRepository;
import com.filmreview.util.SlugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of TitleService for managing movie titles.
 * Implements on-demand fetching: DB miss → TMDB → store → return.
 */
@Service
public class TitleServiceImpl implements TitleService {

  private static final Logger logger = LoggerFactory.getLogger(TitleServiceImpl.class);

  private final TitleRepository titleRepository;
  private final GenreRepository genreRepository;
  private final TitleGenreRepository titleGenreRepository;
  private final TmdbService tmdbService;
  private final TitleMapper titleMapper;

  public TitleServiceImpl(
      TitleRepository titleRepository,
      GenreRepository genreRepository,
      TitleGenreRepository titleGenreRepository,
      TmdbService tmdbService,
      TitleMapper titleMapper) {
    this.titleRepository = titleRepository;
    this.genreRepository = genreRepository;
    this.titleGenreRepository = titleGenreRepository;
    this.tmdbService = tmdbService;
    this.titleMapper = titleMapper;
  }

  @Override
  public Title getTitleById(UUID id) {
    return titleRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Title not found with id: " + id));
  }

  @Override
  public Title getTitleBySlug(String slug) {
    return titleRepository.findBySlug(slug)
        .orElseThrow(() -> new NotFoundException("Title not found with slug: " + slug));
  }

  @Override
  @Transactional
  public Title getTitleByTmdbId(Integer tmdbId) {
    // 1. Check DB first
    return titleRepository.findByTmdbId(tmdbId)
        .orElseGet(() -> {
          // 2. DB miss → Fetch from TMDB
          logger.info("Movie not found in DB, fetching from TMDB: tmdbId={}", tmdbId);
          Title title = fetchAndSaveTitle(tmdbId);
          // 3. Store → Return
          return title;
        });
  }

  @Override
  @Transactional
  public Title fetchAndSaveTitle(Integer tmdbId) {
    TmdbMovieResponse movieResponse = tmdbService.getMovieDetails(tmdbId);
    if (movieResponse == null) {
      throw new NotFoundException("Movie not found in TMDB: " + tmdbId);
    }

    Title title = titleMapper.toTitle(movieResponse);

    // Save title
    title = titleRepository.save(title);

    // Save genres
    saveGenres(title.getId(), movieResponse.getGenres());

    logger.info("Successfully fetched and saved movie: id={}, tmdbId={}",
        title.getId(), tmdbId);
    return title;
  }

  /**
   * Save genres for a title.
   */
  private void saveGenres(UUID titleId, List<TmdbMovieResponse.TmdbGenre> tmdbGenres) {
    if (tmdbGenres == null || tmdbGenres.isEmpty()) {
      return;
    }

    for (TmdbMovieResponse.TmdbGenre movieGenre : tmdbGenres) {
      Integer tmdbGenreId = movieGenre.getId();
      String genreName = movieGenre.getName();

      // Find or create genre
      Genre genre = genreRepository.findById(tmdbGenreId)
          .orElseGet(() -> {
            Genre newGenre = new Genre();
            newGenre.setId(tmdbGenreId);
            newGenre.setName(genreName);
            newGenre.setSlug(SlugUtils.generateSlug(genreName, 100));
            return genreRepository.save(newGenre);
          });

      // Create title-genre relationship if it doesn't exist
      TitleGenreId titleGenreId = new TitleGenreId(titleId, genre.getId());
      if (!titleGenreRepository.existsById(titleGenreId)) {
        TitleGenre titleGenre = new TitleGenre();
        titleGenre.setTitleId(titleId);
        titleGenre.setGenreId(genre.getId());
        titleGenreRepository.save(titleGenre);
      }
    }
  }
}

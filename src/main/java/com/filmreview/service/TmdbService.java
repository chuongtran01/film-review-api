package com.filmreview.service;

import com.filmreview.config.TmdbConfig;
import com.filmreview.dto.tmdb.TmdbGenreInfo;
import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.dto.tmdb.TmdbPageResponse;
import com.filmreview.mapper.TmdbMovieMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbGenre;
import info.movito.themoviedbapi.TmdbMovieLists;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for interacting with TMDB API using the official Java wrapper
 * library.
 * Handles error handling and data transformation.
 */
@Service
public class TmdbService {

  private static final Logger logger = LoggerFactory.getLogger(TmdbService.class);
  private static final String DEFAULT_LANGUAGE = "en-US";

  private final TmdbApi tmdbApi;
  private final TmdbConfig tmdbConfig;
  private final TmdbMovies tmdbMovies;
  private final TmdbMovieLists tmdbMoviesLists;
  private final TmdbGenre tmdbGenre;
  private final TmdbMovieMapper tmdbMovieMapper;

  public TmdbService(TmdbConfig tmdbConfig, TmdbMovieMapper tmdbMovieMapper) {
    this.tmdbConfig = tmdbConfig;
    this.tmdbMovieMapper = tmdbMovieMapper;
    this.tmdbApi = new TmdbApi(tmdbConfig.getApiKey());
    this.tmdbMovies = tmdbApi.getMovies();
    this.tmdbMoviesLists = tmdbApi.getMovieLists();
    this.tmdbGenre = tmdbApi.getGenre();
  }

  /**
   * Get movie details by TMDB ID.
   */
  public TmdbMovieResponse getMovieDetails(Integer tmdbId) {
    try {
      MovieDb movie = tmdbMovies.getDetails(tmdbId, DEFAULT_LANGUAGE, MovieAppendToResponse.CREDITS);
      return tmdbMovieMapper.toMovieResponse(movie);
    } catch (TmdbException e) {
      logger.warn("Movie not found in TMDB: {}", tmdbId, e);
      return null;
    } catch (Exception e) {
      logger.error("Error fetching movie details from TMDB: {}", tmdbId, e);
      throw new RuntimeException("Failed to fetch movie details from TMDB", e);
    }
  }

  /**
   * Get popular movies as Spring Data Page (pageable).
   */
  public Page<TmdbPageResponse.TmdbMovieItem> getPopularMovies(String language, int page, String region) {
    try {
      MovieResultsPage resultsPage = tmdbMoviesLists.getPopular(language, page, region);
      List<TmdbPageResponse.TmdbMovieItem> items = resultsPage.getResults().stream()
          .map(tmdbMovieMapper::toMovieItem)
          .collect(Collectors.toList());

      // TMDB uses 1-indexed pages, convert to 0-indexed for Spring
      int springPage = resultsPage.getPage() != null ? resultsPage.getPage() - 1 : 0;
      // TMDB typically returns 20 items per page
      int size = 20;
      long totalElements = resultsPage.getTotalResults() != null ? resultsPage.getTotalResults() : 0;

      return new PageImpl<>(
          items,
          PageRequest.of(springPage, size),
          totalElements);
    } catch (Exception e) {
      logger.error("Error fetching popular movies from TMDB", e);
      throw new RuntimeException("Failed to fetch popular movies from TMDB", e);
    }
  }

  /**
   * Get official movie genres list from TMDB.
   */
  public List<TmdbGenreInfo> getMovieGenres() {
    try {
      List<info.movito.themoviedbapi.model.core.Genre> genres = tmdbGenre.getMovieList(DEFAULT_LANGUAGE);
      return genres.stream()
          .map(genre -> new TmdbGenreInfo(genre.getId(), genre.getName()))
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error fetching movie genres from TMDB", e);
      throw new RuntimeException("Failed to fetch movie genres from TMDB", e);
    }
  }

  /**
   * Get official TV series genres list from TMDB.
   */
  public List<TmdbGenreInfo> getTvSeriesGenres() {
    try {
      List<info.movito.themoviedbapi.model.core.Genre> genres = tmdbGenre.getTvList(DEFAULT_LANGUAGE);
      return genres.stream()
          .map(genre -> new TmdbGenreInfo(genre.getId(), genre.getName()))
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error fetching TV series genres from TMDB", e);
      throw new RuntimeException("Failed to fetch TV series genres from TMDB", e);
    }
  }

  /**
   * Get image URL for a given path.
   */
  public String getImageUrl(String path, String size) {
    if (path == null || path.isEmpty()) {
      return null;
    }
    return tmdbConfig.getImageBaseUrl() + "/" + size + path;
  }

}

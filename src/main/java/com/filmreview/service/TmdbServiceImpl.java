package com.filmreview.service;

import com.filmreview.config.TmdbConfig;
import com.filmreview.dto.tmdb.TmdbGenreInfo;
import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.dto.tmdb.TmdbPageResponse;
import com.filmreview.mapper.TmdbMovieMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbGenre;
import info.movito.themoviedbapi.TmdbMovieLists;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.TmdbTvSeriesLists;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.TvSeriesResultsPage;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TmdbService for interacting with TMDB API using the
 * official Java wrapper
 * library.
 * Handles error handling and data transformation.
 */
@Service
public class TmdbServiceImpl implements TmdbService {

  private static final Logger logger = LoggerFactory.getLogger(TmdbServiceImpl.class);
  private static final String DEFAULT_LANGUAGE = "en-US";

  private final TmdbApi tmdbApi;
  private final TmdbConfig tmdbConfig;
  private final TmdbMovies tmdbMovies;
  private final TmdbMovieLists tmdbMoviesLists;
  private final TmdbTvSeries tmdbTvSeries;
  private final TmdbTvSeriesLists tmdbTvSeriesLists;
  private final TmdbGenre tmdbGenre;
  private final TmdbMovieMapper tmdbMovieMapper;

  public TmdbServiceImpl(TmdbConfig tmdbConfig, TmdbMovieMapper tmdbMovieMapper) {
    this.tmdbConfig = tmdbConfig;
    this.tmdbMovieMapper = tmdbMovieMapper;
    this.tmdbApi = new TmdbApi(tmdbConfig.getApiKey());
    this.tmdbMovies = tmdbApi.getMovies();
    this.tmdbMoviesLists = tmdbApi.getMovieLists();
    this.tmdbTvSeries = tmdbApi.getTvSeries();
    this.tmdbTvSeriesLists = tmdbApi.getTvSeriesLists();
    this.tmdbGenre = tmdbApi.getGenre();
  }

  @Override
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

  @Override
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

  @Override
  public Page<TmdbPageResponse.TmdbTvSeriesItem> getPopularTVShows(String language, int page) {
    try {
      TvSeriesResultsPage resultsPage = tmdbTvSeriesLists.getPopular(language, page);
      List<TmdbPageResponse.TmdbTvSeriesItem> items = resultsPage.getResults().stream()
          .map(tvSeries -> {
            TmdbPageResponse.TmdbTvSeriesItem item = new TmdbPageResponse.TmdbTvSeriesItem();
            item.setId(tvSeries.getId());
            item.setName(tvSeries.getName());
            item.setOriginalName(tvSeries.getOriginalName());
            item.setOverview(tvSeries.getOverview());
            item.setPosterPath(tvSeries.getPosterPath());
            item.setBackdropPath(tvSeries.getBackdropPath());
            item.setFirstAirDate(tvSeries.getFirstAirDate());
            item.setVoteAverage(tvSeries.getVoteAverage());
            item.setVoteCount(tvSeries.getVoteCount());
            item.setPopularity(tvSeries.getPopularity());
            item.setOriginalLanguage(tvSeries.getOriginalLanguage());
            if (tvSeries.getGenreIds() != null) {
              item.setGenreIds(tvSeries.getGenreIds());
            }
            return item;
          })
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
      logger.error("Error fetching popular TV shows from TMDB", e);
      throw new RuntimeException("Failed to fetch popular TV shows from TMDB", e);
    }
  }

  @Override
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

  @Override
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

  @Override
  public String getImageUrl(String path, String size) {
    if (path == null || path.isEmpty()) {
      return null;
    }
    // Ensure path starts with / if it doesn't already
    String normalizedPath = path.startsWith("/") ? path : "/" + path;
    return tmdbConfig.getImageBaseUrl() + "/" + size + normalizedPath;
  }
}

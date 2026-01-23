package com.filmreview.mapper;

import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.dto.tmdb.TmdbPageResponse;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.movies.MovieDb;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Mapper for converting TMDB library models to internal DTOs.
 */
@Component
public class TmdbMovieMapper {

  /**
   * Map MovieDb to TmdbMovieResponse.
   */
  public TmdbMovieResponse toMovieResponse(MovieDb movie) {
    if (movie == null) {
      return null;
    }

    TmdbMovieResponse response = new TmdbMovieResponse();
    response.setId(movie.getId());
    response.setTitle(movie.getTitle());
    response.setOriginalTitle(movie.getOriginalTitle());
    response.setOverview(movie.getOverview());
    if (movie.getReleaseDate() != null && !movie.getReleaseDate().isEmpty()) {
      response.setReleaseDate(LocalDate.parse(movie.getReleaseDate()));
    }
    response.setRuntime(movie.getRuntime());
    response.setPosterPath(movie.getPosterPath());
    response.setBackdropPath(movie.getBackdropPath());
    response.setStatus(movie.getStatus());
    response.setImdbId(movie.getImdbID());
    response.setPopularity(movie.getPopularity());
    response.setVoteAverage(movie.getVoteAverage());
    response.setVoteCount(movie.getVoteCount());

    // Map genres
    if (movie.getGenres() != null) {
      response.setGenres(movie.getGenres().stream()
          .map(genre -> {
            TmdbMovieResponse.TmdbGenre g = new TmdbMovieResponse.TmdbGenre();
            g.setId(genre.getId());
            g.setName(genre.getName());
            return g;
          })
          .collect(Collectors.toList()));
    }

    // Map credits if available
    if (movie.getCredits() != null) {
      TmdbMovieResponse.TmdbCredits credits = new TmdbMovieResponse.TmdbCredits();

      if (movie.getCredits().getCast() != null) {
        credits.setCast(movie.getCredits().getCast().stream()
            .map(cast -> {
              TmdbMovieResponse.TmdbCast c = new TmdbMovieResponse.TmdbCast();
              c.setId(cast.getId());
              c.setName(cast.getName());
              c.setCharacter(cast.getCharacter());
              c.setOrderIndex(cast.getOrder());
              return c;
            })
            .collect(Collectors.toList()));
      }

      if (movie.getCredits().getCrew() != null) {
        credits.setCrew(movie.getCredits().getCrew().stream()
            .map(crew -> {
              TmdbMovieResponse.TmdbCrew c = new TmdbMovieResponse.TmdbCrew();
              c.setId(crew.getId());
              c.setName(crew.getName());
              c.setJob(crew.getJob());
              c.setDepartment(crew.getDepartment());
              return c;
            })
            .collect(Collectors.toList()));
      }

      response.setCredits(credits);
    }

    return response;
  }

  /**
   * Map Movie (from MovieResultsPage) to TmdbPageResponse.TmdbMovieItem.
   */
  public TmdbPageResponse.TmdbMovieItem toMovieItem(Movie movie) {
    if (movie == null) {
      return null;
    }

    TmdbPageResponse.TmdbMovieItem item = new TmdbPageResponse.TmdbMovieItem();
    item.setId(movie.getId());
    item.setTitle(movie.getTitle());
    item.setPosterPath(movie.getPosterPath());
    item.setReleaseDate(movie.getReleaseDate());
    item.setPopularity(movie.getPopularity());
    return item;
  }
}

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

    // Map simple fields
    response.setBudget(movie.getBudget());
    response.setHomepage(movie.getHomepage());
    response.setOriginalLanguage(movie.getOriginalLanguage());
    // Revenue is Long in MovieDb, convert to Integer (may lose precision for very
    // large values)
    if (movie.getRevenue() != null) {
      response.setRevenue(movie.getRevenue().intValue());
    }

    // Map production companies
    if (movie.getProductionCompanies() != null) {
      response.setProductionCompanies(movie.getProductionCompanies().stream()
          .map(company -> {
            TmdbMovieResponse.TmdbProductionCompany pc = new TmdbMovieResponse.TmdbProductionCompany();
            pc.setId(company.getId());
            pc.setLogoPath(company.getLogoPath());
            pc.setName(company.getName());
            pc.setOriginCountry(company.getOriginCountry());
            return pc;
          })
          .collect(Collectors.toList()));
    }

    // Map production countries
    if (movie.getProductionCountries() != null) {
      response.setProductionCountries(movie.getProductionCountries().stream()
          .map(country -> {
            TmdbMovieResponse.TmdbProductionCountry pc = new TmdbMovieResponse.TmdbProductionCountry();
            // Try common method name patterns for ISO code
            try {
              java.lang.reflect.Method method = country.getClass().getMethod("getIso31661");
              pc.setIso3166_1((String) method.invoke(country));
            } catch (Exception e) {
              // Fallback: try getIso() or getCountryCode()
              try {
                java.lang.reflect.Method method = country.getClass().getMethod("getIso");
                pc.setIso3166_1((String) method.invoke(country));
              } catch (Exception ex) {
                // If no method found, leave as null
                pc.setIso3166_1(null);
              }
            }
            pc.setName(country.getName());
            return pc;
          })
          .collect(Collectors.toList()));
    }

    // Map spoken languages
    if (movie.getSpokenLanguages() != null) {
      response.setSpokenLanguages(movie.getSpokenLanguages().stream()
          .map(lang -> {
            TmdbMovieResponse.TmdbSpokenLanguage sl = new TmdbMovieResponse.TmdbSpokenLanguage();
            sl.setEnglishName(lang.getEnglishName());
            // Try common method name patterns for ISO code
            try {
              java.lang.reflect.Method method = lang.getClass().getMethod("getIso6391");
              sl.setIso639_1((String) method.invoke(lang));
            } catch (Exception e) {
              // Fallback: try getIso() or getLanguageCode()
              try {
                java.lang.reflect.Method method = lang.getClass().getMethod("getIso");
                sl.setIso639_1((String) method.invoke(lang));
              } catch (Exception ex) {
                // If no method found, leave as null
                sl.setIso639_1(null);
              }
            }
            sl.setName(lang.getName());
            return sl;
          })
          .collect(Collectors.toList()));
    }

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
    item.setAdult(movie.getAdult());
    item.setBackdropPath(movie.getBackdropPath());
    item.setGenreIds(movie.getGenreIds());
    item.setOriginalLanguage(movie.getOriginalLanguage());
    item.setOriginalTitle(movie.getOriginalTitle());
    item.setOverview(movie.getOverview());
    item.setPopularity(movie.getPopularity());
    item.setPosterPath(movie.getPosterPath());
    item.setReleaseDate(movie.getReleaseDate());
    item.setTitle(movie.getTitle());
    item.setVideo(movie.getVideo());
    item.setVoteAverage(movie.getVoteAverage());
    item.setVoteCount(movie.getVoteCount());
    return item;
  }
}

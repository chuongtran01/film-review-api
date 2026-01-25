package com.filmreview.mapper;

import com.filmreview.dto.tmdb.TmdbTvSeriesResponse;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting TMDB TV series library models to internal DTOs.
 */
@Component
public class TmdbTvSeriesMapper {

  private static final Logger logger = LoggerFactory.getLogger(TmdbTvSeriesMapper.class);

  /**
   * Map TvSeriesDb to TmdbTvSeriesResponse.
   */
  public TmdbTvSeriesResponse toTvSeriesResponse(TvSeriesDb tvSeries) {
    if (tvSeries == null) {
      return null;
    }

    TmdbTvSeriesResponse response = new TmdbTvSeriesResponse();
    response.setId(tvSeries.getId());
    response.setName(tvSeries.getName());
    response.setOriginalName(tvSeries.getOriginalName());
    response.setOverview(tvSeries.getOverview());

    // Parse first air date
    if (tvSeries.getFirstAirDate() != null && !tvSeries.getFirstAirDate().isEmpty()) {
      try {
        response.setFirstAirDate(java.time.LocalDate.parse(tvSeries.getFirstAirDate()));
      } catch (Exception e) {
        logger.debug("Failed to parse first air date: {}", tvSeries.getFirstAirDate());
      }
    }

    // Parse last air date
    if (tvSeries.getLastAirDate() != null && !tvSeries.getLastAirDate().isEmpty()) {
      try {
        response.setLastAirDate(java.time.LocalDate.parse(tvSeries.getLastAirDate()));
      } catch (Exception e) {
        logger.debug("Failed to parse last air date: {}", tvSeries.getLastAirDate());
      }
    }

    response.setNumberOfSeasons(tvSeries.getNumberOfSeasons());
    response.setNumberOfEpisodes(tvSeries.getNumberOfEpisodes());
    response.setPosterPath(tvSeries.getPosterPath());
    response.setBackdropPath(tvSeries.getBackdropPath());
    response.setStatus(tvSeries.getStatus());
    response.setTagline(tvSeries.getTagline());
    response.setType(tvSeries.getType());
    response.setPopularity(tvSeries.getPopularity());
    response.setVoteAverage(tvSeries.getVoteAverage());
    response.setVoteCount(tvSeries.getVoteCount());
    response.setAdult(tvSeries.getAdult());
    response.setHomepage(tvSeries.getHomepage());
    response.setInProduction(tvSeries.getInProduction());
    response.setOriginalLanguage(tvSeries.getOriginalLanguage());

    // Map episode run time
    if (tvSeries.getEpisodeRunTime() != null) {
      response.setEpisodeRunTime(tvSeries.getEpisodeRunTime());
    }

    // Map languages
    if (tvSeries.getLanguages() != null) {
      response.setLanguages(tvSeries.getLanguages());
    }

    // Map origin country
    if (tvSeries.getOriginCountry() != null) {
      response.setOriginCountry(tvSeries.getOriginCountry());
    }

    // Map genres
    if (tvSeries.getGenres() != null) {
      response.setGenres(tvSeries.getGenres().stream()
          .map(genre -> {
            TmdbTvSeriesResponse.TmdbGenre g = new TmdbTvSeriesResponse.TmdbGenre();
            g.setId(genre.getId());
            g.setName(genre.getName());
            return g;
          })
          .collect(Collectors.toList()));
    }

    // Map created by
    if (tvSeries.getCreatedBy() != null) {
      response.setCreatedBy(tvSeries.getCreatedBy().stream()
          .map(creator -> {
            TmdbTvSeriesResponse.TmdbCreatedBy cb = new TmdbTvSeriesResponse.TmdbCreatedBy();
            cb.setId(creator.getId());
            cb.setCreditId(creator.getCreditId());
            cb.setName(creator.getName());
            // Gender is an enum, convert to Integer using ordinal (0=Not specified,
            // 1=Female, 2=Male, 3=Non-binary)
            if (creator.getGender() != null) {
              cb.setGender(creator.getGender().ordinal());
            }
            cb.setProfilePath(creator.getProfilePath());
            return cb;
          })
          .collect(Collectors.toList()));
    }

    // Map last episode to air
    if (tvSeries.getLastEpisodeToAir() != null) {
      TmdbTvSeriesResponse.TmdbEpisode episode = new TmdbTvSeriesResponse.TmdbEpisode();
      episode.setId(tvSeries.getLastEpisodeToAir().getId());
      episode.setName(tvSeries.getLastEpisodeToAir().getName());
      episode.setOverview(tvSeries.getLastEpisodeToAir().getOverview());
      episode.setVoteAverage(tvSeries.getLastEpisodeToAir().getVoteAverage());
      episode.setVoteCount(tvSeries.getLastEpisodeToAir().getVoteCount());
      if (tvSeries.getLastEpisodeToAir().getAirDate() != null
          && !tvSeries.getLastEpisodeToAir().getAirDate().isEmpty()) {
        try {
          episode.setAirDate(java.time.LocalDate.parse(tvSeries.getLastEpisodeToAir().getAirDate()));
        } catch (Exception e) {
          logger.debug("Failed to parse episode air date: {}", tvSeries.getLastEpisodeToAir().getAirDate());
        }
      }
      episode.setEpisodeNumber(tvSeries.getLastEpisodeToAir().getEpisodeNumber());
      episode.setProductionCode(tvSeries.getLastEpisodeToAir().getProductionCode());
      episode.setRuntime(tvSeries.getLastEpisodeToAir().getRuntime());
      episode.setSeasonNumber(tvSeries.getLastEpisodeToAir().getSeasonNumber());
      episode.setShowId(tvSeries.getLastEpisodeToAir().getShowId());
      episode.setStillPath(tvSeries.getLastEpisodeToAir().getStillPath());
      response.setLastEpisodeToAir(episode);
    }

    // Map networks
    if (tvSeries.getNetworks() != null) {
      response.setNetworks(tvSeries.getNetworks().stream()
          .map(network -> {
            TmdbTvSeriesResponse.TmdbNetwork n = new TmdbTvSeriesResponse.TmdbNetwork();
            n.setId(network.getId());
            n.setLogoPath(network.getLogoPath());
            n.setName(network.getName());
            n.setOriginCountry(network.getOriginCountry());
            return n;
          })
          .collect(Collectors.toList()));
    }

    // Map production companies
    if (tvSeries.getProductionCompanies() != null) {
      response.setProductionCompanies(tvSeries.getProductionCompanies().stream()
          .map(company -> {
            TmdbTvSeriesResponse.TmdbProductionCompany pc = new TmdbTvSeriesResponse.TmdbProductionCompany();
            pc.setId(company.getId());
            pc.setLogoPath(company.getLogoPath());
            pc.setName(company.getName());
            pc.setOriginCountry(company.getOriginCountry());
            return pc;
          })
          .collect(Collectors.toList()));
    }

    // Map production countries
    if (tvSeries.getProductionCountries() != null) {
      response.setProductionCountries(tvSeries.getProductionCountries().stream()
          .map(country -> {
            TmdbTvSeriesResponse.TmdbProductionCountry pc = new TmdbTvSeriesResponse.TmdbProductionCountry();
            // Try to get ISO code using reflection (similar to movie mapper)
            try {
              java.lang.reflect.Method method = country.getClass().getMethod("getIso31661");
              pc.setIso3166_1((String) method.invoke(country));
            } catch (Exception e) {
              try {
                java.lang.reflect.Method method = country.getClass().getMethod("getIso");
                pc.setIso3166_1((String) method.invoke(country));
              } catch (Exception ex) {
                pc.setIso3166_1(null);
              }
            }
            pc.setName(country.getName());
            return pc;
          })
          .collect(Collectors.toList()));
    }

    // Map seasons
    if (tvSeries.getSeasons() != null) {
      response.setSeasons(tvSeries.getSeasons().stream()
          .map(season -> {
            TmdbTvSeriesResponse.TmdbSeason s = new TmdbTvSeriesResponse.TmdbSeason();
            if (season.getAirDate() != null && !season.getAirDate().isEmpty()) {
              try {
                s.setAirDate(java.time.LocalDate.parse(season.getAirDate()));
              } catch (Exception e) {
                logger.debug("Failed to parse season air date: {}", season.getAirDate());
              }
            }
            s.setEpisodeCount(season.getEpisodeCount());
            s.setId(season.getId());
            s.setName(season.getName());
            s.setOverview(season.getOverview());
            s.setPosterPath(season.getPosterPath());
            s.setSeasonNumber(season.getSeasonNumber());
            // Vote average is Integer in library, convert to Double
            if (season.getVoteAverage() != null) {
              s.setVoteAverage(season.getVoteAverage().doubleValue());
            }
            return s;
          })
          .collect(Collectors.toList()));
    }

    // Map spoken languages
    if (tvSeries.getSpokenLanguages() != null) {
      response.setSpokenLanguages(tvSeries.getSpokenLanguages().stream()
          .map(lang -> {
            TmdbTvSeriesResponse.TmdbSpokenLanguage sl = new TmdbTvSeriesResponse.TmdbSpokenLanguage();
            sl.setEnglishName(lang.getEnglishName());
            // Try to get ISO code using reflection (similar to movie mapper)
            try {
              java.lang.reflect.Method method = lang.getClass().getMethod("getIso6391");
              sl.setIso639_1((String) method.invoke(lang));
            } catch (Exception e) {
              try {
                java.lang.reflect.Method method = lang.getClass().getMethod("getIso");
                sl.setIso639_1((String) method.invoke(lang));
              } catch (Exception ex) {
                sl.setIso639_1(null);
              }
            }
            sl.setName(lang.getName());
            return sl;
          })
          .collect(Collectors.toList()));
    }

    return response;
  }
}

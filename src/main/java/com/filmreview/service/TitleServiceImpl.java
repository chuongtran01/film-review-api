package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbMovieResponse;
import com.filmreview.dto.tmdb.TmdbPageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

  @Override
  public Page<Title> getPopularMovies(String language, int page, String region, Pageable pageable) {
    int requestedPageSize = pageable.getPageSize();
    int requestedPage = pageable.getPageNumber();

    // TMDB returns 20 items per page. Calculate which TMDB page we need
    // If frontend requests page 1 with size 6, we need items 0-5 from TMDB page 1
    // If frontend requests page 2 with size 6, we need items 6-11 from TMDB page 1
    // If frontend requests page 4 with size 6, we need items 18-23, which spans
    // TMDB pages 1-2
    int tmdbPage = (requestedPage * requestedPageSize) / 20 + 1;
    int startIndexInTmdbPage = (requestedPage * requestedPageSize) % 20;

    // Fetch popular movies from TMDB (lightweight metadata only)
    Page<TmdbPageResponse.TmdbMovieItem> tmdbMovies = tmdbService.getPopularMovies(language, tmdbPage, region);

    // For each movie, check if it exists in DB first (to get user ratings)
    // If not in DB, return lightweight version without saving
    // Only save to DB when user views details (via getTitleByTmdbId)
    List<Title> allTitles = tmdbMovies.getContent().stream()
        .map(tmdbMovie -> {
          // Check if title exists in DB (has user ratings, etc.)
          return titleRepository.findByTmdbId(tmdbMovie.getId())
              .orElseGet(() -> {
                // Not in DB yet - return lightweight version without saving
                return tmdbMovieItemToTitle(tmdbMovie);
              });
        })
        .collect(Collectors.toList());

    // If we need items beyond the current TMDB page, fetch the next page
    int endIndex = startIndexInTmdbPage + requestedPageSize;
    if (endIndex > allTitles.size() && tmdbMovies.hasNext()) {
      // Fetch next TMDB page to get more items
      Page<TmdbPageResponse.TmdbMovieItem> nextTmdbPage = tmdbService.getPopularMovies(language, tmdbPage + 1, region);
      List<Title> nextPageTitles = nextTmdbPage.getContent().stream()
          .map(tmdbMovie -> {
            return titleRepository.findByTmdbId(tmdbMovie.getId())
                .orElseGet(() -> tmdbMovieItemToTitle(tmdbMovie));
          })
          .collect(Collectors.toList());
      allTitles.addAll(nextPageTitles);
    }

    // Slice to the requested page size
    int fromIndex = Math.min(startIndexInTmdbPage, allTitles.size());
    int toIndex = Math.min(fromIndex + requestedPageSize, allTitles.size());
    List<Title> slicedTitles = allTitles.subList(fromIndex, toIndex);

    // Return as Page with requested pagination info
    return new PageImpl<>(
        slicedTitles,
        pageable,
        tmdbMovies.getTotalElements());
  }

  /**
   * Convert TmdbMovieItem to Title entity without saving to database.
   * This is used for list views. Full details are fetched and saved only when
   * user views details.
   */
  private Title tmdbMovieItemToTitle(TmdbPageResponse.TmdbMovieItem tmdbMovie) {
    Title title = new Title();
    title.setType(Title.TitleType.movie);
    title.setTmdbId(tmdbMovie.getId());
    title.setTitle(tmdbMovie.getTitle());
    title.setOriginalTitle(tmdbMovie.getOriginalTitle());
    title.setSynopsis(tmdbMovie.getOverview());

    // Parse release date if available
    if (tmdbMovie.getReleaseDate() != null && !tmdbMovie.getReleaseDate().isEmpty()) {
      try {
        title.setReleaseDate(java.time.LocalDate.parse(tmdbMovie.getReleaseDate()));
      } catch (Exception e) {
        logger.debug("Failed to parse release date: {}", tmdbMovie.getReleaseDate());
      }
    }

    // Set poster URL if available
    if (tmdbMovie.getPosterPath() != null && !tmdbMovie.getPosterPath().isEmpty()) {
      title.setPosterUrl(tmdbService.getImageUrl(tmdbMovie.getPosterPath(), "w500"));
    }

    // Set backdrop URL if available
    if (tmdbMovie.getBackdropPath() != null && !tmdbMovie.getBackdropPath().isEmpty()) {
      title.setBackdropUrl(tmdbService.getImageUrl(tmdbMovie.getBackdropPath(), "w1920"));
    }

    // Generate slug from title for URL routing
    if (tmdbMovie.getTitle() != null) {
      title.setSlug(com.filmreview.util.SlugUtils.generateSlug(tmdbMovie.getTitle()));
    }

    // Set TMDB vote average and count (scale 0-10, convert to BigDecimal)
    if (tmdbMovie.getVoteAverage() != null) {
      title.setUserRatingAvg(java.math.BigDecimal.valueOf(tmdbMovie.getVoteAverage()));
    }
    if (tmdbMovie.getVoteCount() != null) {
      title.setUserRatingCount(tmdbMovie.getVoteCount());
    } else {
      title.setUserRatingCount(0);
    }

    // Note: This Title object is not persisted. It will be saved when user views
    // details
    // via getTitleByTmdbId() which calls fetchAndSaveTitle()

    return title;
  }
}

package com.filmreview.controller;

import com.filmreview.dto.TitleDto;
import com.filmreview.entity.Title;
import com.filmreview.mapper.TitleDtoMapper;
import com.filmreview.service.TitleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing titles (movies, TV shows).
 */
@RestController
@RequestMapping("/api/v1/titles")
@PreAuthorize("permitAll()")
public class TitleController {

  private static final String DEFAULT_LANGUAGE = "en-US";
  private static final String DEFAULT_REGION = "US";

  private final TitleService titleService;
  private final TitleDtoMapper titleDtoMapper;

  public TitleController(TitleService titleService, TitleDtoMapper titleDtoMapper) {
    this.titleService = titleService;
    this.titleDtoMapper = titleDtoMapper;
  }

  /**
   * Get titles with optional filtering and sorting.
   * GET /api/v1/titles
   */
  @GetMapping
  public ResponseEntity<Page<TitleDto>> getTitles(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Integer genre,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) Double min_rating,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "20") int pageSize,
      @RequestParam(required = false) String sort) {

    // If sort is "popular", use TMDB getPopularMovies or getPopularTVShows
    if ("popular".equals(sort)) {
      Pageable pageable = PageRequest.of(page - 1, pageSize); // Convert to 0-indexed
      if ("movie".equals(type)) {
        Page<Title> titlesPage = titleService.getPopularMovies(DEFAULT_LANGUAGE, page, DEFAULT_REGION, pageable);
        Page<TitleDto> dtoPage = titleDtoMapper.toDtoPage(titlesPage);
        return ResponseEntity.ok(dtoPage);
      } else if ("tv_show".equals(type)) {
        Page<Title> titlesPage = titleService.getPopularTVShows(DEFAULT_LANGUAGE, page, pageable);
        Page<TitleDto> dtoPage = titleDtoMapper.toDtoPage(titlesPage);
        return ResponseEntity.ok(dtoPage);
      }
    }

    // TODO: Implement other sort options and filtering
    // For now, return empty page for other cases
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<TitleDto> emptyPage = new PageImpl<>(java.util.Collections.emptyList(), pageable, 0);
    return ResponseEntity.ok(emptyPage);
  }

  /**
   * Get title by identifier (slug or TMDB ID) with optional type.
   * GET /api/v1/titles/{identifier}?type={movie|tv_show}
   * 
   * - If identifier is numeric: treats as TMDB ID, requires type parameter
   * - If identifier is not numeric: treats as slug, type is optional (can infer
   * from DB)
   */
  @GetMapping("/{identifier}")
  public ResponseEntity<TitleDto> getTitleByIdentifier(
      @PathVariable String identifier,
      @RequestParam(required = false) String type) {

    try {
      // Try to parse as integer (TMDB ID)
      try {
        Integer tmdbId = Integer.parseInt(identifier);

        // Type is required for TMDB ID lookup
        if (type == null) {
          return ResponseEntity.badRequest().build();
        }

        // Route to appropriate service method based on type
        if ("movie".equals(type) || "tv_show".equals(type)) {
          Title title = titleService.getTitleByTmdbId(tmdbId, type);
          TitleDto dto = titleDtoMapper.toDto(title);
          return ResponseEntity.ok(dto);
        } else {
          return ResponseEntity.badRequest().build();
        }
      } catch (NumberFormatException e) {
        // Not numeric, treat as slug
        // Type is optional - can be inferred from DB record
        Title title = titleService.getTitleBySlug(identifier);
        TitleDto dto = titleDtoMapper.toDto(title);
        return ResponseEntity.ok(dto);
      }
    } catch (com.filmreview.exception.NotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}

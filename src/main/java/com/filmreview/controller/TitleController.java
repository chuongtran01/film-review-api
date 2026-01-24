package com.filmreview.controller;

import com.filmreview.entity.Title;
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

  public TitleController(TitleService titleService) {
    this.titleService = titleService;
  }

  /**
   * Get titles with optional filtering and sorting.
   * GET /api/v1/titles
   */
  @GetMapping
  public ResponseEntity<Page<Title>> getTitles(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Integer genre,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) Double min_rating,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "20") int pageSize,
      @RequestParam(required = false) String sort) {

    // If sort is "popular", use TMDB getPopularMovies
    if ("popular".equals(sort) && "movie".equals(type)) {
      Pageable pageable = PageRequest.of(page - 1, pageSize); // Convert to 0-indexed
      Page<Title> titlesPage = titleService.getPopularMovies(DEFAULT_LANGUAGE, page, DEFAULT_REGION, pageable);
      return ResponseEntity.ok(titlesPage);
    }

    // TODO: Implement other sort options and filtering
    // For now, return empty page for other cases
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Title> emptyPage = new PageImpl<>(java.util.Collections.emptyList(), pageable, 0);
    return ResponseEntity.ok(emptyPage);
  }

  /**
   * Get title by ID.
   * GET /api/v1/titles/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<Title> getTitleById(@PathVariable String id) {
    // TODO: Implement when needed
    return ResponseEntity.notFound().build();
  }
}

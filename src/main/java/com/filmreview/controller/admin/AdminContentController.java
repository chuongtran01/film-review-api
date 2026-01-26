package com.filmreview.controller.admin;

import com.filmreview.dto.GenreDto;
import com.filmreview.mapper.GenreDtoMapper;
import com.filmreview.service.GenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin content management controller.
 * Handles movie management.
 * All endpoints require appropriate roles and permissions.
 */
@RestController
@RequestMapping("/api/v1/admin/titles")
public class AdminContentController {

  private final GenreService genreService;
  private final GenreDtoMapper genreDtoMapper;

  public AdminContentController(GenreService genreService, GenreDtoMapper genreDtoMapper) {
    this.genreService = genreService;
    this.genreDtoMapper = genreDtoMapper;
  }

  @PostMapping("/movie")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR') and hasAuthority('titles.create')")
  public ResponseEntity<Map<String, Object>> addMovie(@RequestBody Map<String, Object> movieData) {
    // TODO: Implement when Title entity is created
    return ResponseEntity.ok(Map.of(
        "message", "Movie added successfully",
        "data", movieData));
  }

  @PutMapping("/{titleId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR') and hasAuthority('titles.update')")
  public ResponseEntity<Map<String, Object>> updateTitle(
      @PathVariable String titleId,
      @RequestBody Map<String, Object> titleData) {
    // TODO: Implement when Title entity is created
    return ResponseEntity.ok(Map.of(
        "message", "Title updated successfully",
        "titleId", titleId,
        "data", titleData));
  }

  @DeleteMapping("/{titleId}")
  @PreAuthorize("hasRole('ADMIN') and hasAuthority('titles.delete')")
  public ResponseEntity<Map<String, Object>> deleteTitle(@PathVariable String titleId) {
    // TODO: Implement when Title entity is created
    return ResponseEntity.ok(Map.of(
        "message", "Title deleted successfully",
        "titleId", titleId));
  }

  /**
   * Get all genres.
   * Returns a list of all genres in the system.
   */
  @GetMapping("/genres")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
  public ResponseEntity<List<GenreDto>> getGenres() {
    List<GenreDto> genres = genreDtoMapper.toDtoList(genreService.getAllGenres());
    return ResponseEntity.ok(genres);
  }

  /**
   * Sync movie and TV series genres from TMDB.
   * Fetches official genre list and updates the database.
   */
  @PostMapping("/genres/sync")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR') and hasAuthority('titles.create')")
  public ResponseEntity<Map<String, Object>> syncGenres() {
    int movieGenresSynced = genreService.syncMovieGenres();
    int tvSeriesGenresSynced = genreService.syncTvSeriesGenres();

    return ResponseEntity.ok(Map.of(
        "message", "Movie and TV series genres synced successfully",
        "movieGenresSynced", movieGenresSynced,
        "tvSeriesGenresSynced", tvSeriesGenresSynced));
  }
}

package com.filmreview.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin content management controller.
 * Handles movie management.
 * All endpoints require appropriate roles and permissions.
 */
@RestController
@RequestMapping("/api/v1/admin/titles")
public class AdminContentController {

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
}

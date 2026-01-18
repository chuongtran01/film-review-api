package com.filmreview.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin dashboard controller.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

  @GetMapping
  public ResponseEntity<Map<String, Object>> getDashboard() {
    return ResponseEntity.ok(Map.of(
        "message", "Admin dashboard",
        "stats", Map.of(
            "totalUsers", 0,
            "totalTitles", 0,
            "totalReviews", 0)));
  }
}

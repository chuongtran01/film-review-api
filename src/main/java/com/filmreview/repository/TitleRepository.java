package com.filmreview.repository;

import com.filmreview.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Title entity.
 */
@Repository
public interface TitleRepository extends JpaRepository<Title, UUID> {

  /**
   * Find title by TMDB ID.
   */
  Optional<Title> findByTmdbId(Integer tmdbId);

  /**
   * Find title by slug.
   */
  Optional<Title> findBySlug(String slug);

  /**
   * Check if title exists by TMDB ID.
   */
  boolean existsByTmdbId(Integer tmdbId);

  /**
   * Find titles by type, ordered by release date descending.
   */
  @Query("SELECT t FROM Title t WHERE t.type = :type ORDER BY t.releaseDate DESC")
  java.util.List<Title> findByTypeOrderByReleaseDateDesc(@Param("type") Title.TitleType type);

  /**
   * Find titles by type, ordered by user rating average descending.
   */
  @Query("SELECT t FROM Title t WHERE t.type = :type AND t.userRatingAvg IS NOT NULL ORDER BY t.userRatingAvg DESC")
  java.util.List<Title> findByTypeOrderByUserRatingAvgDesc(@Param("type") Title.TitleType type);

  /**
   * Search titles by query string (searches in title and originalTitle).
   * Case-insensitive partial match.
   */
  @Query("SELECT t FROM Title t WHERE " +
      "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "LOWER(t.originalTitle) LIKE LOWER(CONCAT('%', :query, '%'))")
  org.springframework.data.domain.Page<Title> searchTitles(
      @Param("query") String query,
      org.springframework.data.domain.Pageable pageable);

  /**
   * Search titles by query string and type.
   * Case-insensitive partial match.
   */
  @Query("SELECT t FROM Title t WHERE t.type = :type AND " +
      "(LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "LOWER(t.originalTitle) LIKE LOWER(CONCAT('%', :query, '%')))")
  org.springframework.data.domain.Page<Title> searchTitlesByType(
      @Param("query") String query,
      @Param("type") Title.TitleType type,
      org.springframework.data.domain.Pageable pageable);
}

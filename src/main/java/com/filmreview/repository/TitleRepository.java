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
}

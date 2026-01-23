package com.filmreview.repository;

import com.filmreview.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Genre entity.
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

  /**
   * Find genre by slug.
   */
  Optional<Genre> findBySlug(String slug);

  /**
   * Find genre by name.
   */
  Optional<Genre> findByName(String name);
}

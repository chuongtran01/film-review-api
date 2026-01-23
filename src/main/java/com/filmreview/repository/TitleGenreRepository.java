package com.filmreview.repository;

import com.filmreview.entity.TitleGenre;
import com.filmreview.entity.TitleGenreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for TitleGenre junction entity.
 */
@Repository
public interface TitleGenreRepository extends JpaRepository<TitleGenre, TitleGenreId> {

  /**
   * Find all genres for a title.
   */
  @Query("SELECT tg.genre FROM TitleGenre tg WHERE tg.titleId = :titleId")
  List<com.filmreview.entity.Genre> findGenresByTitleId(@Param("titleId") UUID titleId);

  /**
   * Find all titles for a genre.
   */
  @Query("SELECT tg.title FROM TitleGenre tg WHERE tg.genreId = :genreId")
  List<com.filmreview.entity.Title> findTitlesByGenreId(@Param("genreId") Integer genreId);

  /**
   * Delete all genre associations for a title.
   */
  void deleteByTitleId(UUID titleId);
}

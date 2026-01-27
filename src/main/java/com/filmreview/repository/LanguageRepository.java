package com.filmreview.repository;

import com.filmreview.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Language entity.
 */
@Repository
public interface LanguageRepository extends JpaRepository<Language, String> {

  /**
   * Find language by English name.
   */
  Optional<Language> findByEnglishName(String englishName);
}

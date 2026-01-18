package com.filmreview.repository;

import com.filmreview.entity.Watchlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {

  /**
   * Count watchlist items for a specific user.
   */
  long countByUserId(UUID userId);

  /**
   * Find all watchlist items for a specific user.
   */
  Page<Watchlist> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

  /**
   * Find a specific watchlist item by user and title.
   */
  Optional<Watchlist> findByUserIdAndTitleId(UUID userId, UUID titleId);
}

package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Watchlist entity representing a user's watchlist item for a title.
 */
@Entity
@Table(name = "watchlist", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "title_id" })
})
@Getter
@Setter
@NoArgsConstructor
public class Watchlist extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "title_id", nullable = false)
  private UUID titleId;

  @Column(name = "status", nullable = false, columnDefinition = "watchlist_status")
  @Enumerated(EnumType.STRING)
  private WatchlistStatus status = WatchlistStatus.WANT_TO_WATCH;

  public enum WatchlistStatus {
    WANT_TO_WATCH,
    WATCHING,
    COMPLETED,
    DROPPED
  }
}

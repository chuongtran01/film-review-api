package com.filmreview.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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

  @Column(name = "status", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private WatchlistStatus status = WatchlistStatus.WANT_TO_WATCH;

  public enum WatchlistStatus {
    WANT_TO_WATCH("want_to_watch"),
    WATCHING("watching"),
    COMPLETED("completed"),
    DROPPED("dropped");

    private final String value;

    WatchlistStatus(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @JsonCreator
    public static WatchlistStatus fromValue(String value) {
      for (WatchlistStatus status : WatchlistStatus.values()) {
        if (status.value.equalsIgnoreCase(value)) {
          return status;
        }
      }
      // Fallback: try to match by enum name (uppercase)
      try {
        return valueOf(value.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Unknown watchlist status: " + value);
      }
    }
  }
}

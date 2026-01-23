package com.filmreview.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Junction entity for Title-Genre many-to-many relationship.
 */
@Entity
@Table(name = "title_genres", indexes = {
    @Index(name = "idx_title_genres_title_id", columnList = "title_id"),
    @Index(name = "idx_title_genres_genre_id", columnList = "genre_id")
})
@Getter
@Setter
@NoArgsConstructor
@IdClass(TitleGenreId.class)
public class TitleGenre {

  @Id
  @Column(name = "title_id", nullable = false)
  private UUID titleId;

  @Id
  @Column(name = "genre_id", nullable = false)
  private Integer genreId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "title_id", insertable = false, updatable = false)
  private Title title;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "genre_id", insertable = false, updatable = false)
  private Genre genre;
}

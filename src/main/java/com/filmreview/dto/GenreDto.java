package com.filmreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for Genre entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenreDto {
  private Integer id;
  private String name;
  private String slug;
}

package com.filmreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for Language entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDto {
  private String iso6391;
  private String englishName;
  private String name;
}

package com.filmreview.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

  @Size(max = 100, message = "Display name must not exceed 100 characters")
  private String displayName;

  @Size(max = 2000, message = "Bio must not exceed 2000 characters")
  private String bio;

  @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
  private String avatarUrl;
}

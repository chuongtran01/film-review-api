package com.filmreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

  private UserDto user;
  private String accessToken;
  private String refreshToken;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private Boolean verified;
    private LocalDateTime createdAt;
    private List<String> roles;
    private List<String> permissions;
  }
}

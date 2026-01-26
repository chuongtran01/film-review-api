package com.filmreview.controller;

import com.filmreview.dto.AuthResponse;
import com.filmreview.dto.LoginRequest;
import com.filmreview.dto.RegisterRequest;
import com.filmreview.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody RegisterRequest request,
      HttpServletResponse response) {
    AuthResponse authResponse = authService.register(request);
    setRefreshTokenCookie(response, authResponse.getRefreshToken());
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletResponse response) {
    AuthResponse authResponse = authService.login(request);
    setRefreshTokenCookie(response, authResponse.getRefreshToken());
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @CookieValue(value = "refresh_token", required = false) String refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      AuthResponse authResponse = authService.refreshToken(refreshToken);
      setRefreshTokenCookie(response, authResponse.getRefreshToken());
      return ResponseEntity.ok(authResponse);
    } catch (com.filmreview.exception.UnauthorizedException e) {
      // Clear refresh token cookie if refresh fails
      Cookie cookie = new Cookie("refresh_token", null);
      cookie.setHttpOnly(true);
      cookie.setSecure(false);
      cookie.setPath("/");
      cookie.setMaxAge(0);
      response.addCookie(cookie);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(
      @CookieValue(value = "refresh_token", required = false) String refreshToken,
      HttpServletResponse httpResponse) {
    if (refreshToken != null) {
      authService.logout(refreshToken);
    }

    // Clear refresh token cookie
    Cookie cookie = new Cookie("refresh_token", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    httpResponse.addCookie(cookie);

    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
  }

  private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie("refresh_token", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Set to true in production with HTTPS
    cookie.setPath("/");
    cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
    response.addCookie(cookie);
  }
}

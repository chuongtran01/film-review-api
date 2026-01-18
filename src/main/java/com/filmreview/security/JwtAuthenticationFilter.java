package com.filmreview.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider tokenProvider;

  public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    try {
      String jwt = getJwtFromRequest(request);

      if (jwt != null && tokenProvider.validateToken(jwt)) {
        UUID userId = tokenProvider.getUserIdFromToken(jwt);
        String username = tokenProvider.getUsernameFromToken(jwt);
        String email = tokenProvider.getEmailFromToken(jwt);

        UserPrincipal userPrincipal = new UserPrincipal(userId, username, email);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      logger.error("Cannot set user authentication", e);
    }

    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}

package com.filmreview.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
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

    String jwt = getJwtFromRequest(request);

    // If no token is present, let Spring Security handle it (will return 401 via
    // entry point)
    if (jwt == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Check if token is valid (not expired and properly formatted)
      if (tokenProvider.validateToken(jwt)) {
        UUID userId = tokenProvider.getUserIdFromToken(jwt);
        String username = tokenProvider.getUsernameFromToken(jwt);
        String email = tokenProvider.getEmailFromToken(jwt);
        List<String> roles = tokenProvider.getRolesFromToken(jwt);
        List<String> permissions = tokenProvider.getPermissionsFromToken(jwt);

        UserPrincipal userPrincipal = new UserPrincipal(userId, username, email, roles, permissions);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } else {
        // Token is present but invalid/expired - return 401
        handleUnauthorized(response, "Token expired or invalid");
        return;
      }
    } catch (ExpiredJwtException e) {
      // Token is expired - return 401 so frontend can refresh
      handleUnauthorized(response, "Token expired");
      return;
    } catch (JwtException e) {
      // Token is malformed or invalid - return 401
      handleUnauthorized(response, "Invalid token");
      return;
    } catch (Exception e) {
      // Other errors - return 401
      logger.error("Cannot set user authentication", e);
      handleUnauthorized(response, "Authentication failed");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(
        String.format("{\"code\":\"UNAUTHORIZED\",\"message\":\"%s\",\"details\":null}", message));
    response.getWriter().flush();
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}

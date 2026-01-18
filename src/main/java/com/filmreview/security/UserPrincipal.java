package com.filmreview.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserPrincipal that implements UserDetails.
 * Contains essential user information extracted from JWT token.
 * 
 * This avoids database lookups on every request while providing
 * all necessary user information to controllers.
 */
public class UserPrincipal implements UserDetails {

  private final UUID id;
  private final String username;
  private final String email;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserPrincipal(UUID id, String username, String email) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return null; // Not needed for JWT authentication
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

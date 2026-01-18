package com.filmreview.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
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
  private final List<String> roles;
  private final List<String> permissions;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserPrincipal(UUID id, String username, String email, List<String> roles, List<String> permissions) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.roles = roles != null ? roles : List.of("USER");
    this.permissions = permissions != null ? permissions : List.of();

    // Build authorities collection:
    // 1. Add roles with ROLE_ prefix (for hasRole checks)
    // 2. Add permissions as-is (for hasAuthority checks)
    List<GrantedAuthority> authorityList = new java.util.ArrayList<>();

    // Add role authorities (ROLE_USER, ROLE_ADMIN, etc.)
    this.roles.forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));

    // Add permission authorities (titles.create, titles.delete, etc.)
    this.permissions.forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission)));

    this.authorities = java.util.Collections.unmodifiableList(authorityList);
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public List<String> getRoles() {
    return roles;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public boolean hasPermission(String permission) {
    return permissions != null && permissions.contains(permission);
  }

  public boolean hasAnyPermission(String... permissionsToCheck) {
    if (permissions == null) {
      return false;
    }
    for (String permissionToCheck : permissionsToCheck) {
      if (hasPermission(permissionToCheck)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasRole(String role) {
    return roles != null && roles.stream()
        .anyMatch(r -> r.equalsIgnoreCase(role));
  }

  public boolean hasAnyRole(String... rolesToCheck) {
    if (roles == null) {
      return false;
    }
    for (String roleToCheck : rolesToCheck) {
      if (hasRole(roleToCheck)) {
        return true;
      }
    }
    return false;
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

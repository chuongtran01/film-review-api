package com.filmreview.entity;

/**
 * Enum representing user roles in the system.
 */
public enum RoleType {
  USER("USER", "User", "Default role for regular users"),
  MODERATOR("MODERATOR", "Moderator", "Role for content moderators"),
  ADMIN("ADMIN", "Administrator", "Full administrative access");

  private final String name;
  private final String displayName;
  private final String description;

  RoleType(String name, String displayName, String description) {
    this.name = name;
    this.displayName = displayName;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Get RoleType from string name (case-insensitive).
   */
  public static RoleType fromName(String name) {
    if (name == null) {
      return null;
    }
    for (RoleType roleType : RoleType.values()) {
      if (roleType.name.equalsIgnoreCase(name)) {
        return roleType;
      }
    }
    throw new IllegalArgumentException("Unknown role type: " + name);
  }
}

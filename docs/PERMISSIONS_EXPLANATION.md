# Understanding `hasAuthority` and Permission-Based Access Control

## Overview

Spring Security uses **authorities** (also called **permissions**) to control access to resources. There are two main concepts:

1. **Roles** (`hasRole`): High-level groupings (e.g., `ROLE_ADMIN`, `ROLE_USER`)
2. **Authorities/Permissions** (`hasAuthority`): Specific actions (e.g., `titles.create`, `titles.delete`)

## How `hasAuthority` Works

`hasAuthority('permission_name')` checks if the user's `GrantedAuthority` collection contains an authority with that exact name.

### Example:
```java
@PreAuthorize("hasAuthority('titles.create')")
public ResponseEntity<?> createTitle() {
    // Only users with 'titles.create' permission can access
}
```

Spring Security checks:
1. Gets the authenticated user's `UserDetails` (our `UserPrincipal`)
2. Calls `getAuthorities()` to get the collection of `GrantedAuthority`
3. Checks if any authority matches `'titles.create'`
4. Allows access if found, denies otherwise

## Current Implementation

### What We Have:
- **Roles** are stored in JWT token and converted to authorities with `ROLE_` prefix
- **Permissions** are stored in database but NOT loaded into JWT token yet

### Two Approaches:

#### Option 1: Store Permissions in JWT Token (Recommended for Performance)
- **Pros**: No database lookup on each request, faster
- **Cons**: Larger token size, permissions cached until token expires
- **Use when**: Permissions don't change frequently

#### Option 2: Load Permissions from Database on Each Request
- **Pros**: Always up-to-date permissions, smaller token
- **Cons**: Database lookup on every request (performance impact)
- **Use when**: Permissions change frequently

## Implementation

We'll use **Option 1** (store in JWT) for better performance. The token will include:
- User roles (e.g., `["USER", "ADMIN"]`)
- User permissions (e.g., `["titles.create", "titles.delete", "users.manage"]`)

Both are converted to `GrantedAuthority` objects in `UserPrincipal`.

## Usage Examples

### Role-Based Access:
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
```

### Permission-Based Access:
```java
@PreAuthorize("hasAuthority('titles.create')")
@PreAuthorize("hasAnyAuthority('titles.create', 'titles.update')")
```

### Combined (Role + Permission):
```java
@PreAuthorize("hasRole('ADMIN') and hasAuthority('titles.delete')")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR') and hasAuthority('titles.create')")
```

### Complex Expressions:
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('MODERATOR') and hasAuthority('reviews.moderate'))")
```

## Permission Naming Convention

We use dot notation for permissions:
- `titles.create` - Create titles
- `titles.update` - Update titles
- `titles.delete` - Delete titles
- `titles.view` - View titles
- `users.manage` - Manage users
- `reviews.moderate` - Moderate reviews
- `reviews.delete` - Delete reviews
- `admin.access` - Full admin access

This makes it easy to group related permissions and check them programmatically.

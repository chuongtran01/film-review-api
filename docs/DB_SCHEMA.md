# Database Schema: Film Review Platform

## Overview

This document defines the complete database schema, including tables, relationships, constraints, and indexes for the film review platform.

**Database**: PostgreSQL 15+
**ORM**: Spring Data JPA / Hibernate (Java)

---

## 1. Core Entities

### Users Table

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Required for MVP (email/password auth)
    display_name VARCHAR(100),
    avatar_url TEXT,
    bio TEXT,
    verified BOOLEAN DEFAULT FALSE,
    -- OAuth fields (added in V1 migration)
    oauth_provider VARCHAR(50), -- 'google', 'facebook', or NULL
    oauth_provider_id VARCHAR(255), -- Provider's user ID
    oauth_email VARCHAR(255), -- Email from OAuth provider
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_active_at TIMESTAMP WITH TIME ZONE
);

-- OAuth index (added in V1 migration)
-- CREATE UNIQUE INDEX idx_users_oauth_provider_id ON users(oauth_provider, oauth_provider_id) 
-- WHERE oauth_provider IS NOT NULL;

-- OAuth constraint (added in V1 migration)
-- ALTER TABLE users ADD CONSTRAINT check_auth_method CHECK (
--     (password_hash IS NOT NULL) OR (oauth_provider IS NOT NULL)
-- );

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
```

### Titles Table (Movies & TV Shows)

```sql
CREATE TYPE title_type AS ENUM ('movie', 'tv_show');

CREATE TABLE titles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type title_type NOT NULL,
    tmdb_id INTEGER UNIQUE NOT NULL,
    imdb_id VARCHAR(20),
    title VARCHAR(500) NOT NULL,
    original_title VARCHAR(500),
    slug VARCHAR(500) UNIQUE NOT NULL,
    synopsis TEXT,
    release_date DATE,
    runtime INTEGER, -- minutes
    poster_url TEXT,
    backdrop_url TEXT,
    status VARCHAR(50), -- released, upcoming, etc.
    -- Aggregated scores (cached)
    user_rating_avg NUMERIC(3, 2), -- 0.00 to 10.00
    user_rating_count INTEGER DEFAULT 0,
    -- TV show specific (nullable for movies)
    number_of_seasons INTEGER,
    number_of_episodes INTEGER,
    first_air_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_titles_tmdb_id ON titles(tmdb_id);
CREATE INDEX idx_titles_imdb_id ON titles(imdb_id) WHERE imdb_id IS NOT NULL;
CREATE INDEX idx_titles_slug ON titles(slug);
CREATE INDEX idx_titles_type ON titles(type);
CREATE INDEX idx_titles_release_date ON titles(release_date DESC);
CREATE INDEX idx_titles_type_release ON titles(type, release_date DESC);
CREATE INDEX idx_titles_rating_avg ON titles(user_rating_avg DESC) WHERE user_rating_avg IS NOT NULL;

-- Full-text search index
CREATE INDEX idx_titles_search ON titles 
USING gin(to_tsvector('english', COALESCE(title, '') || ' ' || COALESCE(original_title, '') || ' ' || COALESCE(synopsis, '')));
```

### Episodes Table (TV Shows Only)

```sql
CREATE TABLE episodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tv_show_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    season_number INTEGER NOT NULL,
    episode_number INTEGER NOT NULL,
    title VARCHAR(500),
    synopsis TEXT,
    air_date DATE,
    runtime INTEGER, -- minutes
    tmdb_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(tv_show_id, season_number, episode_number)
);

CREATE INDEX idx_episodes_tv_show_id ON episodes(tv_show_id);
CREATE INDEX idx_episodes_tv_show_season ON episodes(tv_show_id, season_number);
```

### People Table (Cast & Crew)

```sql
CREATE TABLE people (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tmdb_id INTEGER UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    bio TEXT,
    birth_date DATE,
    death_date DATE,
    profile_url TEXT,
    known_for_department VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_people_tmdb_id ON people(tmdb_id);
CREATE INDEX idx_people_slug ON people(slug);
CREATE INDEX idx_people_name ON people(name);

-- Full-text search index
CREATE INDEX idx_people_search ON people 
USING gin(to_tsvector('english', name));
```

### Title-Person Relationship (Cast & Crew)

```sql
CREATE TYPE person_role AS ENUM ('cast', 'director', 'writer', 'producer', 'composer', 'cinematographer', 'editor');

CREATE TABLE title_people (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES people(id) ON DELETE CASCADE,
    role person_role NOT NULL,
    character_name VARCHAR(255), -- for cast members
    order_index INTEGER, -- for cast ordering
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(title_id, person_id, role)
);

CREATE INDEX idx_title_people_title_id ON title_people(title_id);
CREATE INDEX idx_title_people_person_id ON title_people(person_id);
CREATE INDEX idx_title_people_role ON title_people(role);
CREATE INDEX idx_title_people_title_role ON title_people(title_id, role);
```

### Genres Table

```sql
CREATE TABLE genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_genres_slug ON genres(slug);
```

### Title-Genre Relationship

```sql
CREATE TABLE title_genres (
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (title_id, genre_id)
);

CREATE INDEX idx_title_genres_title_id ON title_genres(title_id);
CREATE INDEX idx_title_genres_genre_id ON title_genres(genre_id);
```

### Alternate Titles Table

```sql
CREATE TABLE alternate_titles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    language_code VARCHAR(10), -- ISO 639-1
    country_code VARCHAR(10), -- ISO 3166-1
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_alternate_titles_title_id ON alternate_titles(title_id);
CREATE INDEX idx_alternate_titles_title ON alternate_titles(title);
```

---

## 2. User-Generated Content

### Ratings Table

```sql
CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, title_id)
);

CREATE INDEX idx_ratings_user_id ON ratings(user_id);
CREATE INDEX idx_ratings_title_id ON ratings(title_id);
CREATE INDEX idx_ratings_user_title ON ratings(user_id, title_id);
CREATE INDEX idx_ratings_title_score ON ratings(title_id, score);
CREATE INDEX idx_ratings_title_created ON ratings(title_id, created_at DESC);
CREATE INDEX idx_ratings_user_created ON ratings(user_id, created_at DESC);
```

### Reviews Table

```sql
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    rating_id UUID REFERENCES ratings(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    contains_spoilers BOOLEAN DEFAULT FALSE,
    helpful_count INTEGER DEFAULT 0,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, title_id)
);

CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_title_id ON reviews(title_id);
CREATE INDEX idx_reviews_user_title ON reviews(user_id, title_id);
CREATE INDEX idx_reviews_title_created ON reviews(title_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_title_helpful ON reviews(title_id, helpful_count DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_user_created ON reviews(user_id, created_at DESC) WHERE deleted_at IS NULL;

-- Full-text search index
CREATE INDEX idx_reviews_search ON reviews 
USING gin(to_tsvector('english', content)) 
WHERE deleted_at IS NULL;
```

### Review Helpful Votes Table

```sql
CREATE TABLE review_helpful (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, review_id)
);

CREATE INDEX idx_review_helpful_user_id ON review_helpful(user_id);
CREATE INDEX idx_review_helpful_review_id ON review_helpful(review_id);
```

### Watchlist Table

```sql
CREATE TYPE watchlist_status AS ENUM ('want_to_watch', 'watching', 'completed', 'dropped');

CREATE TABLE watchlist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    status watchlist_status NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, title_id)
);

CREATE INDEX idx_watchlist_user_id ON watchlist(user_id);
CREATE INDEX idx_watchlist_title_id ON watchlist(title_id);
CREATE INDEX idx_watchlist_user_status ON watchlist(user_id, status);
CREATE INDEX idx_watchlist_user_updated ON watchlist(user_id, updated_at DESC);
```

### Lists Table (Custom Collections)

```sql
CREATE TABLE lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_lists_user_id ON lists(user_id);
CREATE INDEX idx_lists_user_public ON lists(user_id, is_public);
```

### List Items Table

```sql
CREATE TABLE list_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id UUID NOT NULL REFERENCES lists(id) ON DELETE CASCADE,
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    order_index INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(list_id, title_id)
);

CREATE INDEX idx_list_items_list_id ON list_items(list_id);
CREATE INDEX idx_list_items_title_id ON list_items(title_id);
CREATE INDEX idx_list_items_list_order ON list_items(list_id, order_index);
```

---

## 3. Platform & Streaming

### Platforms Table (Streaming Services)

```sql
CREATE TABLE platforms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    logo_url TEXT,
    website_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_platforms_slug ON platforms(slug);
```

### Title-Platform Relationship (Where to Watch)

```sql
CREATE TABLE title_platforms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    platform_id INTEGER NOT NULL REFERENCES platforms(id) ON DELETE CASCADE,
    url TEXT,
    available_from DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(title_id, platform_id)
);

CREATE INDEX idx_title_platforms_title_id ON title_platforms(title_id);
CREATE INDEX idx_title_platforms_platform_id ON title_platforms(platform_id);
```

---

## 4. Relationships Summary

### Entity Relationships

```
User (1) ──→ (N) Rating
User (1) ──→ (N) Review
User (1) ──→ (N) Watchlist
User (1) ──→ (N) List
User (1) ──→ (N) ReviewHelpful

Title (1) ──→ (N) Rating
Title (1) ──→ (N) Review
Title (1) ──→ (N) Episode (TV shows only)
Title (N) ←─→ (N) Person (via title_people)
Title (N) ←─→ (N) Genre (via title_genres)
Title (N) ←─→ (N) Platform (via title_platforms)
Title (1) ──→ (N) AlternateTitle

Review (1) ──→ (0..1) Rating (optional)
Review (1) ──→ (N) ReviewHelpful

List (1) ──→ (N) ListItem
ListItem (N) ──→ (1) Title
```

### Foreign Key Constraints

- All foreign keys use `ON DELETE CASCADE` for related data
- `reviews.rating_id` uses `ON DELETE SET NULL` (review can exist without rating)
- Unique constraints prevent duplicates (one rating/review per user per title)

---

## 5. Database Functions & Triggers

### Update Aggregated Scores Trigger

```sql
-- Function to update title rating aggregates (incremental approach)
-- Uses mathematical formulas instead of full recalculation for O(1) performance
CREATE OR REPLACE FUNCTION update_title_rating_aggregates()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Add new rating to average: (avg * count + new_score) / (count + 1)
        UPDATE titles
        SET 
            user_rating_avg = (
                (user_rating_avg * user_rating_count + NEW.score) / 
                (user_rating_count + 1)
            ),
            user_rating_count = user_rating_count + 1
        WHERE id = NEW.title_id;
        RETURN NEW;
        
    ELSIF TG_OP = 'UPDATE' THEN
        -- Adjust average for changed score: (avg * count - old_score + new_score) / count
        UPDATE titles
        SET 
            user_rating_avg = (
                (user_rating_avg * user_rating_count - OLD.score + NEW.score) / 
                user_rating_count
            )
        WHERE id = NEW.title_id;
        RETURN NEW;
        
    ELSIF TG_OP = 'DELETE' THEN
        -- Remove rating from average: (avg * count - old_score) / (count - 1)
        UPDATE titles
        SET 
            user_rating_avg = CASE 
                WHEN user_rating_count > 1 THEN
                    (user_rating_avg * user_rating_count - OLD.score) / 
                    (user_rating_count - 1)
                ELSE 0  -- No ratings left
            END,
            user_rating_count = user_rating_count - 1
        WHERE id = OLD.title_id;
        RETURN OLD;
    END IF;
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Trigger on ratings insert/update/delete
CREATE TRIGGER trigger_update_rating_aggregates
AFTER INSERT OR UPDATE OR DELETE ON ratings
FOR EACH ROW
EXECUTE FUNCTION update_title_rating_aggregates();

-- Note: For production, consider adding a periodic validation cron job
-- (e.g., hourly) to recalculate aggregates from scratch as a safety net
-- to catch any edge cases or floating-point precision drift
```

### Update Review Helpful Count Trigger

```sql
-- Function to update review helpful count
CREATE OR REPLACE FUNCTION update_review_helpful_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE reviews
        SET helpful_count = helpful_count + 1
        WHERE id = NEW.review_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE reviews
        SET helpful_count = helpful_count - 1
        WHERE id = OLD.review_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger on review_helpful insert/delete
CREATE TRIGGER trigger_update_helpful_count
AFTER INSERT OR DELETE ON review_helpful
FOR EACH ROW
EXECUTE FUNCTION update_review_helpful_count();
```

### Update Timestamps (Spring Data JPA Auditing)

**Note:** Instead of database triggers, we use Spring Data JPA Auditing for better performance and maintainability.

**Implementation in Java:**

1. Enable JPA Auditing in the main application class:
```java
@SpringBootApplication
@EnableJpaAuditing
public class FilmReviewBackendApplication {
    // ...
}
```

2. Use BaseEntity class (recommended) or add annotations directly:

**Option A: Extend BaseEntity (Recommended)**
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    // createdAt and updatedAt are inherited automatically
    // ... other fields
}
```

**Option B: Add annotations directly**
```java
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    // ... other fields
}
```

**Benefits:**
- No database trigger overhead (~0.5ms saved per update)
- Automatic timestamp management via JPA lifecycle
- Works seamlessly with Spring Data JPA
- Better testability and debugging
- Consistent across all entities

**Alternative (if needed):** Database triggers can be used as a fallback for direct SQL updates, but JPA Auditing is the primary approach.

---

## 6. Indexes Summary

### Critical Indexes

**Lookup Indexes**:
- All primary keys (automatic)
- All foreign keys
- Unique constraints (username, email, slug, etc.)

**Search Indexes**:
- Full-text search on `titles` (title, original_title, synopsis)
- Full-text search on `people` (name)
- Full-text search on `reviews` (content)

**Sorting Indexes**:
- `ratings(title_id, created_at DESC)`
- `reviews(title_id, created_at DESC)`
- `reviews(title_id, helpful_count DESC)`
- `titles(release_date DESC)`
- `titles(user_rating_avg DESC)`

**Composite Indexes**:
- `ratings(user_id, title_id)` - unique constraint
- `titles(type, release_date DESC)` - common filter
- `watchlist(user_id, status)` - user's watchlist by status

---

## 7. Data Integrity Rules

### Constraints

1. **Unique Constraints**:
   - One rating per user per title
   - One review per user per title
   - One watchlist entry per user per title
   - One list item per list per title

2. **Check Constraints**:
   - Rating score: 1-10
   - Review content: minimum length (enforced in application)

3. **Foreign Key Constraints**:
   - All foreign keys enforced
   - Cascade deletes for related data
   - Set null for optional relationships

### Data Validation

**Application-Level** (enforced in code):
- Email format validation
- Username format (alphanumeric + underscore, 3-50 chars)
- Review content length (50-5000 characters)
- Password strength requirements

**Database-Level**:
- NOT NULL constraints
- CHECK constraints (rating score range)
- UNIQUE constraints
- Foreign key constraints

---

## 8. Migration Strategy

### Initial Migration

1. Create all tables
2. Create indexes
3. Create functions and triggers
4. Seed genres table
5. Seed platforms table (if known)

### Future Migrations

- Use Liquibase for version control
- Test migrations on staging first
- Backup database before production migrations
- Document breaking changes
- Spring Boot auto-runs migrations on startup

---

## 9. Performance Considerations

### Query Optimization

- Use indexes for all WHERE and ORDER BY clauses
- Limit result sets with pagination
- Use SELECT only needed columns
- Avoid N+1 queries (use JOINs or eager loading)

### Maintenance

- Regular VACUUM and ANALYZE
- Monitor index usage (drop unused indexes)
- Monitor table sizes
- Archive old data if needed (future)

---

*This schema is a living document. Update as requirements evolve.*

-- liquibase formatted sql

-- changeset chuong.tran:1
-- comment: Create ENUM types
CREATE TYPE title_type AS ENUM ('movie', 'tv_show');
CREATE TYPE person_role AS ENUM ('cast', 'director', 'writer', 'producer', 'composer', 'cinematographer', 'editor');
CREATE TYPE watchlist_status AS ENUM ('want_to_watch', 'watching', 'completed', 'dropped');

-- changeset chuong.tran:2
-- comment: Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url TEXT,
    bio TEXT,
    verified BOOLEAN DEFAULT FALSE,
    oauth_provider VARCHAR(50),
    oauth_provider_id VARCHAR(255),
    oauth_email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_active_at TIMESTAMP WITH TIME ZONE
);

-- changeset chuong.tran:3
-- comment: Create titles table
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
    runtime INTEGER,
    poster_url TEXT,
    backdrop_url TEXT,
    status VARCHAR(50),
    user_rating_avg NUMERIC(3, 2),
    user_rating_count INTEGER DEFAULT 0,
    number_of_seasons INTEGER,
    number_of_episodes INTEGER,
    first_air_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- changeset chuong.tran:4
-- comment: Create episodes table
CREATE TABLE episodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tv_show_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    season_number INTEGER NOT NULL,
    episode_number INTEGER NOT NULL,
    title VARCHAR(500),
    synopsis TEXT,
    air_date DATE,
    runtime INTEGER,
    tmdb_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(tv_show_id, season_number, episode_number)
);

-- changeset chuong.tran:5
-- comment: Create people table
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

-- changeset chuong.tran:6
-- comment: Create title_people table
CREATE TABLE title_people (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES people(id) ON DELETE CASCADE,
    role person_role NOT NULL,
    character_name VARCHAR(255),
    order_index INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(title_id, person_id, role)
);

-- changeset chuong.tran:7
-- comment: Create genres table
CREATE TABLE genres (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- changeset chuong.tran:8
-- comment: Create title_genres table
CREATE TABLE title_genres (
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (title_id, genre_id)
);

-- changeset chuong.tran:9
-- comment: Create alternate_titles table
CREATE TABLE alternate_titles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    language_code VARCHAR(10),
    country_code VARCHAR(10),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- changeset chuong.tran:10
-- comment: Create ratings table
CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, title_id)
);

-- changeset chuong.tran:11
-- comment: Create reviews table
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

-- changeset chuong.tran:12
-- comment: Create review_helpful table
CREATE TABLE review_helpful (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, review_id)
);

-- changeset chuong.tran:13
-- comment: Create watchlist table
CREATE TABLE watchlist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    status watchlist_status NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, title_id)
);

-- changeset chuong.tran:14
-- comment: Create lists table
CREATE TABLE lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- changeset chuong.tran:15
-- comment: Create list_items table
CREATE TABLE list_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id UUID NOT NULL REFERENCES lists(id) ON DELETE CASCADE,
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    order_index INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(list_id, title_id)
);

-- changeset chuong.tran:16
-- comment: Create platforms table
CREATE TABLE platforms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    logo_url TEXT,
    website_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- changeset chuong.tran:17
-- comment: Create title_platforms table
CREATE TABLE title_platforms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_id UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    platform_id INTEGER NOT NULL REFERENCES platforms(id) ON DELETE CASCADE,
    url TEXT,
    available_from DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(title_id, platform_id)
);

-- changeset chuong.tran:18
-- comment: Create indexes for users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- changeset chuong.tran:19
-- comment: Create indexes for titles table
CREATE INDEX idx_titles_tmdb_id ON titles(tmdb_id);
CREATE INDEX idx_titles_imdb_id ON titles(imdb_id) WHERE imdb_id IS NOT NULL;
CREATE INDEX idx_titles_slug ON titles(slug);
CREATE INDEX idx_titles_type ON titles(type);
CREATE INDEX idx_titles_release_date ON titles(release_date DESC);
CREATE INDEX idx_titles_type_release ON titles(type, release_date DESC);
CREATE INDEX idx_titles_rating_avg ON titles(user_rating_avg DESC) WHERE user_rating_avg IS NOT NULL;
CREATE INDEX idx_titles_search ON titles 
USING gin(to_tsvector('english', COALESCE(title, '') || ' ' || COALESCE(original_title, '') || ' ' || COALESCE(synopsis, '')));

-- changeset chuong.tran:20
-- comment: Create indexes for episodes table
CREATE INDEX idx_episodes_tv_show_id ON episodes(tv_show_id);
CREATE INDEX idx_episodes_tv_show_season ON episodes(tv_show_id, season_number);

-- changeset chuong.tran:21
-- comment: Create indexes for people table
CREATE INDEX idx_people_tmdb_id ON people(tmdb_id);
CREATE INDEX idx_people_slug ON people(slug);
CREATE INDEX idx_people_name ON people(name);
CREATE INDEX idx_people_search ON people 
USING gin(to_tsvector('english', name));

-- changeset chuong.tran:22
-- comment: Create indexes for title_people table
CREATE INDEX idx_title_people_title_id ON title_people(title_id);
CREATE INDEX idx_title_people_person_id ON title_people(person_id);
CREATE INDEX idx_title_people_role ON title_people(role);
CREATE INDEX idx_title_people_title_role ON title_people(title_id, role);

-- changeset chuong.tran:23
-- comment: Create indexes for genres table
CREATE INDEX idx_genres_slug ON genres(slug);

-- changeset chuong.tran:24
-- comment: Create indexes for title_genres table
CREATE INDEX idx_title_genres_title_id ON title_genres(title_id);
CREATE INDEX idx_title_genres_genre_id ON title_genres(genre_id);

-- changeset chuong.tran:25
-- comment: Create indexes for alternate_titles table
CREATE INDEX idx_alternate_titles_title_id ON alternate_titles(title_id);
CREATE INDEX idx_alternate_titles_title ON alternate_titles(title);

-- changeset chuong.tran:26
-- comment: Create indexes for ratings table
CREATE INDEX idx_ratings_user_id ON ratings(user_id);
CREATE INDEX idx_ratings_title_id ON ratings(title_id);
CREATE INDEX idx_ratings_user_title ON ratings(user_id, title_id);
CREATE INDEX idx_ratings_title_score ON ratings(title_id, score);
CREATE INDEX idx_ratings_title_created ON ratings(title_id, created_at DESC);
CREATE INDEX idx_ratings_user_created ON ratings(user_id, created_at DESC);

-- changeset chuong.tran:27
-- comment: Create indexes for reviews table
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_title_id ON reviews(title_id);
CREATE INDEX idx_reviews_user_title ON reviews(user_id, title_id);
CREATE INDEX idx_reviews_title_created ON reviews(title_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_title_helpful ON reviews(title_id, helpful_count DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_user_created ON reviews(user_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_search ON reviews 
USING gin(to_tsvector('english', content)) 
WHERE deleted_at IS NULL;

-- changeset chuong.tran:28
-- comment: Create indexes for review_helpful table
CREATE INDEX idx_review_helpful_user_id ON review_helpful(user_id);
CREATE INDEX idx_review_helpful_review_id ON review_helpful(review_id);

-- changeset chuong.tran:29
-- comment: Create indexes for watchlist table
CREATE INDEX idx_watchlist_user_id ON watchlist(user_id);
CREATE INDEX idx_watchlist_title_id ON watchlist(title_id);
CREATE INDEX idx_watchlist_user_status ON watchlist(user_id, status);
CREATE INDEX idx_watchlist_user_updated ON watchlist(user_id, updated_at DESC);

-- changeset chuong.tran:30
-- comment: Create indexes for lists table
CREATE INDEX idx_lists_user_id ON lists(user_id);
CREATE INDEX idx_lists_user_public ON lists(user_id, is_public);

-- changeset chuong.tran:31
-- comment: Create indexes for list_items table
CREATE INDEX idx_list_items_list_id ON list_items(list_id);
CREATE INDEX idx_list_items_title_id ON list_items(title_id);
CREATE INDEX idx_list_items_list_order ON list_items(list_id, order_index);

-- changeset chuong.tran:32
-- comment: Create indexes for platforms table
CREATE INDEX idx_platforms_slug ON platforms(slug);

-- changeset chuong.tran:33
-- comment: Create indexes for title_platforms table
CREATE INDEX idx_title_platforms_title_id ON title_platforms(title_id);
CREATE INDEX idx_title_platforms_platform_id ON title_platforms(platform_id);

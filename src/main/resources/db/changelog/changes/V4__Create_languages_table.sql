-- liquibase formatted sql

-- changeset chuong.tran:4
-- comment: Create languages table
CREATE TABLE languages (
    iso_639_1 VARCHAR(10) PRIMARY KEY,
    english_name VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_languages_iso_639_1 ON languages(iso_639_1);

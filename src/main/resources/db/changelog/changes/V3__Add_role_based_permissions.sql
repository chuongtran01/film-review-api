-- liquibase formatted sql

-- changeset chuong.tran:1
-- comment: Create permissions table
CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_permissions_name ON permissions(name);

-- changeset chuong.tran:2
-- comment: Create roles table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_roles_name ON roles(name);

-- changeset chuong.tran:3
-- comment: Create role_permissions junction table
CREATE TABLE role_permissions (
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- changeset chuong.tran:4
-- comment: Create user_roles junction table
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    UNIQUE(user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- changeset chuong.tran:5
-- comment: Insert default permissions
INSERT INTO permissions (name, display_name, description) VALUES
    ('titles.create', 'Create Titles', 'Permission to create new movies and TV shows'),
    ('titles.update', 'Update Titles', 'Permission to update existing titles'),
    ('titles.delete', 'Delete Titles', 'Permission to delete titles'),
    ('titles.view', 'View Titles', 'Permission to view titles'),
    ('users.view', 'View Users', 'Permission to view user profiles'),
    ('users.manage', 'Manage Users', 'Permission to manage users (promote, demote, etc.)'),
    ('reviews.moderate', 'Moderate Reviews', 'Permission to moderate user reviews'),
    ('reviews.delete', 'Delete Reviews', 'Permission to delete any review'),
    ('admin.access', 'Admin Access', 'Full administrative access to the system');

-- changeset chuong.tran:6
-- comment: Insert default roles
INSERT INTO roles (name, display_name, description) VALUES
    ('USER', 'User', 'Default role for regular users'),
    ('MODERATOR', 'Moderator', 'Role for content moderators'),
    ('ADMIN', 'Administrator', 'Full administrative access');

-- changeset chuong.tran:7
-- comment: Assign permissions to USER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER'
  AND p.name IN ('titles.view', 'users.view');

-- changeset chuong.tran:8
-- comment: Assign permissions to MODERATOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MODERATOR'
  AND p.name IN ('titles.view', 'titles.create', 'titles.update', 'users.view', 'reviews.moderate', 'reviews.delete');

-- changeset chuong.tran:9
-- comment: Assign permissions to ADMIN role (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- changeset chuong.tran:10
-- comment: Assign USER role to all existing users who don't have any role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE r.name = 'USER'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
  );

CREATE TABLE users(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_users_email_normalized CHECK ( email =  LOWER(TRIM(email)) ),
    CONSTRAINT chk_users_account_status CHECK ( account_status IN ('ACTIVE', 'DISABLED'))
);

CREATE TABLE roles(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(20) NOT NULL UNIQUE
);

CREATE TABLE permissions(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE user_roles(
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions(
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('MODERATOR'),
    ('USER');

INSERT INTO permissions (name)
VALUES
    ('USER_READ'),
    ('USER_DISABLE'),
    ('SESSION_REVOKE'),
    ('ROLE_ASSIGN');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM (
    VALUES
        ('ADMIN', 'USER_READ'),
        ('ADMIN', 'USER_DISABLE'),
        ('ADMIN', 'SESSION_REVOKE'),
        ('ADMIN', 'ROLE_ASSIGN'),
        ('MODERATOR', 'USER_READ'),
        ('MODERATOR', 'SESSION_REVOKE')
     ) AS mapping(role_name, permission_name)
JOIN roles r ON r.name = mapping.role_name
JOIN permissions p ON p.name = mapping.permission_name;
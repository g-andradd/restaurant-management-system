-- TIMESTAMP WITH TIME ZONE is used instead of plain TIMESTAMP so that timezone information
-- is preserved. Java's Instant (always UTC) maps correctly to TIMESTAMP WITH TIME ZONE on
-- both H2 (PostgreSQL compatibility mode) and PostgreSQL. Plain TIMESTAMP silently discards
-- the timezone offset and can corrupt round-trips when the DB server timezone differs from UTC.
--
-- Note on the name index: a functional index on LOWER(name) would be ideal for the
-- case-insensitive search, but H2 2.x does not support expression indexes even in PostgreSQL
-- compatibility mode. A plain index on name is used here; case-insensitivity is handled by
-- the Spring Data query (LOWER(name) LIKE LOWER(?)).

CREATE TABLE users (
    id            UUID                     NOT NULL,
    name          VARCHAR(150)             NOT NULL,
    email         VARCHAR(255)             NOT NULL,
    login         VARCHAR(60)              NOT NULL,
    password_hash VARCHAR(255)             NOT NULL,
    role          VARCHAR(30)              NOT NULL,
    street        VARCHAR(150)             NOT NULL,
    number        VARCHAR(20)              NOT NULL,
    city          VARCHAR(100)             NOT NULL,
    zip_code      VARCHAR(20)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_users_email ON users (email);
CREATE        INDEX idx_users_name  ON users (name);

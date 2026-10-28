-- ============ USERS ============
CREATE TABLE users (
                       id          BIGSERIAL PRIMARY KEY,
                       username    VARCHAR(50)  NOT NULL UNIQUE,
                       email       VARCHAR(100) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
                       enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                       CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
);

-- ============ POSTS ============
CREATE TABLE posts (
                       id          BIGSERIAL PRIMARY KEY,
                       user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       title       VARCHAR(255),
                       content     TEXT         NOT NULL,
                       created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE post_images (
                             id          BIGSERIAL PRIMARY KEY,
                             post_id     BIGINT       NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                             file_name   VARCHAR(255) NOT NULL,
                             file_url    VARCHAR(500) NOT NULL,
                             created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============ COMMENTS ============
CREATE TABLE comments (
                          id          BIGSERIAL PRIMARY KEY,
                          post_id     BIGINT       NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                          user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          content     TEXT,
                          created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                          updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE comment_images (
                                id          BIGSERIAL PRIMARY KEY,
                                comment_id  BIGINT       NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
                                file_name   VARCHAR(255) NOT NULL,
                                file_url    VARCHAR(500) NOT NULL,
                                created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============ REACTIONS ============
CREATE TABLE reactions (
                           id          BIGSERIAL PRIMARY KEY,
                           post_id     BIGINT       NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                           user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           type        VARCHAR(20)  NOT NULL,
                           created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                           updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                           CONSTRAINT uq_reactions_user_post UNIQUE (user_id, post_id)
);

-- ============ INDEXES ============
CREATE INDEX idx_posts_user_id        ON posts(user_id);
CREATE INDEX idx_posts_created_at     ON posts(created_at DESC);
CREATE INDEX idx_post_images_post_id  ON post_images(post_id);
CREATE INDEX idx_comments_post_id     ON comments(post_id);
CREATE INDEX idx_comment_images_cmt   ON comment_images(comment_id);
CREATE INDEX idx_reactions_post_id    ON reactions(post_id);

-- ============ ADMIN MẶC ĐỊNH ============
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (username, email, password, role, enabled)
VALUES ('admin', 'admin@blog.com', crypt('Admin@123', gen_salt('bf', 10)), 'ADMIN', TRUE);
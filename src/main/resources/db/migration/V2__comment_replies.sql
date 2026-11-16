ALTER TABLE comments
    ADD COLUMN parent_id BIGINT REFERENCES comments (id) ON DELETE CASCADE,
    ADD COLUMN reply_to_user_id BIGINT REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_comments_parent_id ON comments (parent_id);
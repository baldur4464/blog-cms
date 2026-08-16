-- ADR-013: publishedAt is set once when a post first transitions to PUBLISHED
ALTER TABLE posts ADD COLUMN published_at TIMESTAMPTZ;

CREATE INDEX ix_post_published_at ON posts (published_at);

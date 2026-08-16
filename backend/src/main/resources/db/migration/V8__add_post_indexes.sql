-- ADR-010: performance indexes on posts table
CREATE INDEX ix_post_status     ON posts (status);
CREATE INDEX ix_post_author_id  ON posts (author_id);
CREATE INDEX ix_post_category_id ON posts (category_id);

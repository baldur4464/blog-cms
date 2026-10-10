ALTER TABLE posts ADD COLUMN cover_media_id UUID;
ALTER TABLE posts ADD CONSTRAINT fk_posts_cover_media
    FOREIGN KEY (cover_media_id) REFERENCES media (id) ON DELETE SET NULL;

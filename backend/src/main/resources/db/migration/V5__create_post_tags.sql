CREATE TABLE post_tags (
    post_id UUID NOT NULL,
    tag_id  UUID NOT NULL,

    CONSTRAINT pk_post_tags     PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag  FOREIGN KEY (tag_id)  REFERENCES tags  (id) ON DELETE CASCADE
);

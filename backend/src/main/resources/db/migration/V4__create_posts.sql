CREATE TABLE posts (
    id          UUID         NOT NULL,
    title       VARCHAR(255) NOT NULL,
    slug        VARCHAR(280) NOT NULL,
    excerpt     VARCHAR(500),
    content     TEXT,
    status      VARCHAR(20)  NOT NULL,
    author_id   UUID         NOT NULL,
    category_id UUID,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_posts        PRIMARY KEY (id),
    CONSTRAINT uq_posts_slug   UNIQUE (slug),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id)   REFERENCES users (id),
    CONSTRAINT fk_posts_cat    FOREIGN KEY (category_id) REFERENCES categories (id)
);

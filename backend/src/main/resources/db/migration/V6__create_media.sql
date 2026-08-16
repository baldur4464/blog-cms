CREATE TABLE media (
    id            UUID         NOT NULL,
    filename      VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    mime_type     VARCHAR(127) NOT NULL,
    size          BIGINT       NOT NULL,
    url           VARCHAR(512) NOT NULL,
    uploaded_by   UUID         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_media             PRIMARY KEY (id),
    CONSTRAINT fk_media_uploader    FOREIGN KEY (uploaded_by) REFERENCES users (id)
);

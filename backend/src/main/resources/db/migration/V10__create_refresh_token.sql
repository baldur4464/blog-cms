-- ADR-014: opaque refresh tokens stored as SHA-256 hashes
CREATE TABLE refresh_token (
    id          UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    token_hash  VARCHAR(64) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_refresh_token         PRIMARY KEY (id),
    CONSTRAINT ux_refresh_token_hash    UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user    FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX ix_refresh_token_user_id ON refresh_token (user_id);

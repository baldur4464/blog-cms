-- ADR-010: user is a reserved word in PostgreSQL; table renamed to app_user
ALTER TABLE users RENAME TO app_user;

ALTER TABLE app_user RENAME CONSTRAINT pk_users TO pk_app_user;
ALTER TABLE app_user RENAME CONSTRAINT uq_users_username TO ux_app_user_username;
ALTER TABLE app_user RENAME CONSTRAINT uq_users_email TO ux_app_user_email;

-- Update FK in posts
ALTER TABLE posts RENAME CONSTRAINT fk_posts_author TO fk_post_author;

-- Update FK in media
ALTER TABLE media RENAME CONSTRAINT fk_media_uploader TO fk_media_uploaded_by;

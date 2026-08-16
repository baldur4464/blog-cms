-- ADR-018 Kapitel 5 & 14: Posts werden mehrsprachig (Translation-Group-Modell)

ALTER TABLE posts ADD COLUMN locale               VARCHAR(35);
ALTER TABLE posts ADD COLUMN translation_group_id UUID;

-- Backfill: bestehende Posts der Default-Locale zuordnen, je Zeile eigene Gruppe
UPDATE posts SET locale = 'de' WHERE locale IS NULL;
UPDATE posts SET translation_group_id = gen_random_uuid() WHERE translation_group_id IS NULL;

ALTER TABLE posts ALTER COLUMN locale               SET NOT NULL;
ALTER TABLE posts ALTER COLUMN translation_group_id SET NOT NULL;

-- Slug ist ab jetzt pro Locale eindeutig (ADR-018 Kapitel 7), nicht mehr global
ALTER TABLE posts DROP CONSTRAINT uq_posts_slug;
ALTER TABLE posts ADD  CONSTRAINT uq_posts_locale_slug  UNIQUE (locale, slug);

-- Höchstens eine Version je Sprache innerhalb einer Übersetzungsgruppe
ALTER TABLE posts ADD  CONSTRAINT uq_posts_group_locale UNIQUE (translation_group_id, locale);

CREATE INDEX idx_posts_group ON posts (translation_group_id);

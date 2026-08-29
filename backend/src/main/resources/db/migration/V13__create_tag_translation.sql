-- ADR-018 Kapitel 6 & 14: Tag-Übersetzungen (Translation-Table-Modell)

CREATE TABLE tag_translation (
    id         UUID         NOT NULL,
    tag_id     UUID         NOT NULL,
    locale     VARCHAR(35)  NOT NULL,
    name       VARCHAR(80)  NOT NULL,
    slug       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_tag_translation     PRIMARY KEY (id),
    CONSTRAINT fk_tag_tr_tag          FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE,
    CONSTRAINT uq_tag_tr_tag_locale   UNIQUE (tag_id, locale),
    CONSTRAINT uq_tag_tr_locale_slug  UNIQUE (locale, slug)
);

-- Bestehende Tags verlustfrei in die Default-Locale-Übersetzung migrieren
INSERT INTO tag_translation (id, tag_id, locale, name, slug, created_at, updated_at)
SELECT gen_random_uuid(), id, 'de', name, slug, created_at, updated_at
FROM tags;

-- Übersetzbare Spalten aus der Basistabelle entfernen
ALTER TABLE tags DROP CONSTRAINT uq_tags_name;
ALTER TABLE tags DROP CONSTRAINT uq_tags_slug;
ALTER TABLE tags DROP COLUMN name;
ALTER TABLE tags DROP COLUMN slug;

CREATE INDEX idx_tag_tr_tag ON tag_translation (tag_id);

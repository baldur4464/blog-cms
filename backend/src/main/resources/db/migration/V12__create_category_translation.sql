-- ADR-018 Kapitel 6 & 14: Kategorie-Übersetzungen (Translation-Table-Modell)

CREATE TABLE category_translation (
    id          UUID         NOT NULL,
    category_id UUID         NOT NULL,
    locale      VARCHAR(35)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_category_translation   PRIMARY KEY (id),
    CONSTRAINT fk_cat_tr_category        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    CONSTRAINT uq_cat_tr_category_locale UNIQUE (category_id, locale),
    CONSTRAINT uq_cat_tr_locale_slug     UNIQUE (locale, slug)
);

-- Bestehende Kategorien verlustfrei in die Default-Locale-Übersetzung migrieren
INSERT INTO category_translation (id, category_id, locale, name, slug, description, created_at, updated_at)
SELECT gen_random_uuid(), id, 'de', name, slug, description, created_at, updated_at
FROM categories;

-- Übersetzbare Spalten aus der Basistabelle entfernen (eine Quelle der Wahrheit je Sprache)
ALTER TABLE categories DROP CONSTRAINT uq_categories_name;
ALTER TABLE categories DROP CONSTRAINT uq_categories_slug;
ALTER TABLE categories DROP COLUMN name;
ALTER TABLE categories DROP COLUMN slug;
ALTER TABLE categories DROP COLUMN description;

CREATE INDEX idx_cat_tr_category ON category_translation (category_id);

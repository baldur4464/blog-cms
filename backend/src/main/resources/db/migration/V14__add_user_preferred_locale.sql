-- ADR-018 Kapitel 3 & 14 / ADR-005 Patch: persönliche UI-Sprache je Benutzer

ALTER TABLE app_user ADD COLUMN preferred_locale VARCHAR(35);
-- kein Backfill: NULL ⇒ Fallback über Accept-Language / default-locale (ADR-018 Kapitel 8)

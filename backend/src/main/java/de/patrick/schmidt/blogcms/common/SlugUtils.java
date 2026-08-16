package de.patrick.schmidt.blogcms.common;

import java.text.Normalizer;

public final class SlugUtils {

    private SlugUtils() {}

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String result = input.trim().toLowerCase();

        // German umlaut transliteration (ADR-011)
        result = result
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");

        // Remove remaining diacritics via Unicode NFD decomposition
        result = Normalizer.normalize(result, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        // Replace all non-alphanumeric characters with hyphens
        result = result
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return result;
    }

    /** Falls toSlug() einen leeren String liefert, UUID-Fragment als Fallback. */
    public static String toSlugOrFallback(String input, String uuidFragment) {
        String slug = toSlug(input);
        return slug.isEmpty() ? "item-" + uuidFragment.substring(0, 8) : slug;
    }
}

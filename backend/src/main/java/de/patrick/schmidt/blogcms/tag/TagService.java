package de.patrick.schmidt.blogcms.tag;

import de.patrick.schmidt.blogcms.common.SlugUtils;
import de.patrick.schmidt.blogcms.common.exception.BusinessException;
import de.patrick.schmidt.blogcms.common.exception.ConflictException;
import de.patrick.schmidt.blogcms.common.exception.ResourceNotFoundException;
import de.patrick.schmidt.blogcms.i18n.LocaleService;
import de.patrick.schmidt.blogcms.tag.dto.TagRequest;
import de.patrick.schmidt.blogcms.tag.dto.TagResponse;
import de.patrick.schmidt.blogcms.tag.dto.TagTranslationRequest;
import de.patrick.schmidt.blogcms.tag.dto.TagTranslationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagTranslationRepository translationRepository;
    private final LocaleService localeService;

    // ── Public API ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TagResponse> findAllLocalized(String locale) {
        String def = localeService.getDefaultLocale().toLanguageTag();
        return tagRepository.findAll().stream()
                .map(t -> toLocalized(t, locale, def))
                .toList();
    }

    // ── Admin API ─────────────────────────────────────────────────────────────

    /** Legt einen Tag samt seiner verpflichtenden Default-Locale-Übersetzung an (ADR-018 Kapitel 6). */
    @Transactional
    public TagResponse create(TagRequest request) {
        String def = localeService.getDefaultLocale().toLanguageTag();
        if (translationRepository.existsByLocaleAndName(def, request.name())) {
            throw new ConflictException("Tag already exists: " + request.name());
        }
        Tag tag = tagRepository.save(Tag.builder().build());
        TagTranslation tr = TagTranslation.builder()
                .tag(tag)
                .locale(def)
                .name(request.name())
                .slug(uniqueSlug(def, SlugUtils.toSlug(request.name()), null))
                .build();
        translationRepository.save(tr);
        tag.getTranslations().add(tr);
        return toLocalized(tag, def, def);
    }

    @Transactional(readOnly = true)
    public List<TagTranslationResponse> findTranslations(UUID tagId) {
        getOrThrow(tagId);
        return translationRepository.findByTag_Id(tagId).stream()
                .map(TagTranslationResponse::from)
                .toList();
    }

    @Transactional
    public TagTranslationResponse upsertTranslation(UUID tagId, String localeTag, TagTranslationRequest request) {
        Tag tag = getOrThrow(tagId);
        String locale = requireSupportedLocale(localeTag);

        TagTranslation tr = translationRepository.findByTag_IdAndLocale(tagId, locale)
                .orElseGet(() -> TagTranslation.builder().tag(tag).locale(locale).build());

        tr.setName(request.name());
        tr.setSlug(uniqueSlug(locale, SlugUtils.toSlug(request.name()), tr.getId()));
        return TagTranslationResponse.from(translationRepository.save(tr));
    }

    @Transactional
    public void deleteTranslation(UUID tagId, String localeTag) {
        String locale = requireSupportedLocale(localeTag);
        String def = localeService.getDefaultLocale().toLanguageTag();
        if (locale.equals(def)) {
            throw new BusinessException("Default-locale translation cannot be deleted");
        }
        TagTranslation tr = translationRepository.findByTag_IdAndLocale(tagId, locale)
                .orElseThrow(() -> new ResourceNotFoundException("Translation not found: " + locale));
        translationRepository.delete(tr);
    }

    @Transactional
    public void delete(UUID id) {
        getOrThrow(id);
        tagRepository.deleteById(id);
    }

    public Tag getOrThrow(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + id));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private TagResponse toLocalized(Tag t, String locale, String defaultLocale) {
        List<TagTranslation> translations = t.getTranslations();
        Optional<TagTranslation> exact = translations.stream()
                .filter(tr -> tr.getLocale().equals(locale)).findFirst();
        TagTranslation chosen = exact
                .or(() -> translations.stream().filter(tr -> tr.getLocale().equals(defaultLocale)).findFirst())
                .orElse(null);
        List<String> available = translations.stream()
                .map(TagTranslation::getLocale).sorted().toList();
        if (chosen == null) {
            return new TagResponse(t.getId(), locale, null, null, false, available);
        }
        return new TagResponse(t.getId(), locale, chosen.getName(), chosen.getSlug(), exact.isPresent(), available);
    }

    private String requireSupportedLocale(String tag) {
        return localeService.match(tag)
                .map(Locale::toLanguageTag)
                .orElseThrow(() -> new BusinessException("Unsupported locale: " + tag));
    }

    private String uniqueSlug(String locale, String base, UUID selfId) {
        String candidate = base;
        int counter = 2;
        while (true) {
            Optional<TagTranslation> existing = translationRepository.findByLocaleAndSlug(locale, candidate);
            if (existing.isEmpty() || existing.get().getId().equals(selfId)) {
                return candidate;
            }
            candidate = base + "-" + counter++;
        }
    }
}

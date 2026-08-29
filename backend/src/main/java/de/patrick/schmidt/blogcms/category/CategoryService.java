package de.patrick.schmidt.blogcms.category;

import de.patrick.schmidt.blogcms.category.dto.CategoryRequest;
import de.patrick.schmidt.blogcms.category.dto.CategoryResponse;
import de.patrick.schmidt.blogcms.category.dto.CategoryTranslationRequest;
import de.patrick.schmidt.blogcms.category.dto.CategoryTranslationResponse;
import de.patrick.schmidt.blogcms.common.SlugUtils;
import de.patrick.schmidt.blogcms.common.exception.BusinessException;
import de.patrick.schmidt.blogcms.common.exception.ConflictException;
import de.patrick.schmidt.blogcms.common.exception.ResourceNotFoundException;
import de.patrick.schmidt.blogcms.i18n.LocaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryTranslationRepository translationRepository;
    private final LocaleService localeService;

    // ── Public API ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllLocalized(String locale) {
        String def = localeService.getDefaultLocale().toLanguageTag();
        return categoryRepository.findAll().stream()
                .map(c -> toLocalized(c, locale, def))
                .toList();
    }

    // ── Admin API ─────────────────────────────────────────────────────────────

    /** Legt eine Kategorie samt ihrer verpflichtenden Default-Locale-Übersetzung an (ADR-018 Kapitel 6). */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String def = localeService.getDefaultLocale().toLanguageTag();
        if (translationRepository.existsByLocaleAndName(def, request.name())) {
            throw ConflictException.of("error.category.exists", request.name());
        }
        Category category = categoryRepository.save(Category.builder().build());
        CategoryTranslation tr = CategoryTranslation.builder()
                .category(category)
                .locale(def)
                .name(request.name())
                .slug(uniqueSlug(def, SlugUtils.toSlug(request.name()), null))
                .description(request.description())
                .build();
        translationRepository.save(tr);
        category.getTranslations().add(tr);
        return toLocalized(category, def, def);
    }

    @Transactional(readOnly = true)
    public List<CategoryTranslationResponse> findTranslations(UUID categoryId) {
        getOrThrow(categoryId);
        return translationRepository.findByCategory_Id(categoryId).stream()
                .map(CategoryTranslationResponse::from)
                .toList();
    }

    @Transactional
    public CategoryTranslationResponse upsertTranslation(UUID categoryId, String localeTag, CategoryTranslationRequest request) {
        Category category = getOrThrow(categoryId);
        String locale = requireSupportedLocale(localeTag);

        CategoryTranslation tr = translationRepository.findByCategory_IdAndLocale(categoryId, locale)
                .orElseGet(() -> CategoryTranslation.builder().category(category).locale(locale).build());

        tr.setName(request.name());
        tr.setSlug(uniqueSlug(locale, SlugUtils.toSlug(request.name()), tr.getId()));
        tr.setDescription(request.description());
        return CategoryTranslationResponse.from(translationRepository.save(tr));
    }

    @Transactional
    public void deleteTranslation(UUID categoryId, String localeTag) {
        String locale = requireSupportedLocale(localeTag);
        String def = localeService.getDefaultLocale().toLanguageTag();
        if (locale.equals(def)) {
            throw BusinessException.of("error.translation.defaultProtected");
        }
        CategoryTranslation tr = translationRepository.findByCategory_IdAndLocale(categoryId, locale)
                .orElseThrow(() -> ResourceNotFoundException.of("error.translation.notFound", locale));
        translationRepository.delete(tr);
    }

    @Transactional
    public void delete(UUID id) {
        getOrThrow(id);
        categoryRepository.deleteById(id);
    }

    public Category getOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("error.category.notFound", id));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private CategoryResponse toLocalized(Category c, String locale, String defaultLocale) {
        List<CategoryTranslation> translations = c.getTranslations();
        Optional<CategoryTranslation> exact = translations.stream()
                .filter(t -> t.getLocale().equals(locale)).findFirst();
        CategoryTranslation chosen = exact
                .or(() -> translations.stream().filter(t -> t.getLocale().equals(defaultLocale)).findFirst())
                .orElse(null);
        List<String> available = translations.stream()
                .map(CategoryTranslation::getLocale).sorted().toList();
        if (chosen == null) {
            return new CategoryResponse(c.getId(), locale, null, null, null, false, available);
        }
        return new CategoryResponse(c.getId(), locale, chosen.getName(), chosen.getSlug(),
                chosen.getDescription(), exact.isPresent(), available);
    }

    private String requireSupportedLocale(String tag) {
        return localeService.match(tag)
                .map(Locale::toLanguageTag)
                .orElseThrow(() -> BusinessException.of("error.locale.unsupported", tag));
    }

    private String uniqueSlug(String locale, String base, UUID selfId) {
        String candidate = base;
        int counter = 2;
        while (true) {
            Optional<CategoryTranslation> existing = translationRepository.findByLocaleAndSlug(locale, candidate);
            if (existing.isEmpty() || existing.get().getId().equals(selfId)) {
                return candidate;
            }
            candidate = base + "-" + counter++;
        }
    }
}

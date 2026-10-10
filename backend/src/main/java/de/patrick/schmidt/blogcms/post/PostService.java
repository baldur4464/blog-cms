package de.patrick.schmidt.blogcms.post;

import de.patrick.schmidt.blogcms.category.Category;
import de.patrick.schmidt.blogcms.category.CategoryRepository;
import de.patrick.schmidt.blogcms.category.CategoryTranslation;
import de.patrick.schmidt.blogcms.category.CategoryTranslationRepository;
import de.patrick.schmidt.blogcms.common.PageResponse;
import de.patrick.schmidt.blogcms.common.SlugUtils;
import de.patrick.schmidt.blogcms.common.exception.BusinessException;
import de.patrick.schmidt.blogcms.common.exception.ConflictException;
import de.patrick.schmidt.blogcms.common.exception.ResourceNotFoundException;
import de.patrick.schmidt.blogcms.i18n.LocaleService;
import de.patrick.schmidt.blogcms.media.Media;
import de.patrick.schmidt.blogcms.media.MediaRepository;
import de.patrick.schmidt.blogcms.post.dto.*;
import de.patrick.schmidt.blogcms.post.event.PostArchivedEvent;
import de.patrick.schmidt.blogcms.post.event.PostPublishedEvent;
import de.patrick.schmidt.blogcms.tag.Tag;
import de.patrick.schmidt.blogcms.tag.TagRepository;
import de.patrick.schmidt.blogcms.tag.TagTranslation;
import de.patrick.schmidt.blogcms.tag.TagTranslationRepository;
import de.patrick.schmidt.blogcms.user.Role;
import de.patrick.schmidt.blogcms.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryTranslationRepository categoryTranslationRepository;
    private final TagRepository tagRepository;
    private final TagTranslationRepository tagTranslationRepository;
    private final MediaRepository mediaRepository;
    private final LocaleService localeService;
    private final ApplicationEventPublisher eventPublisher;

    // ── Public API ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> findPublished(String categorySlug, String tagSlug, String locale, String q, Pageable pageable) {
        UUID categoryId = null;
        UUID tagId = null;

        if (categorySlug != null) {
            Optional<UUID> found = categoryTranslationRepository.findByLocaleAndSlug(locale, categorySlug)
                    .map(t -> t.getCategory().getId());
            if (found.isEmpty()) {
                return PageResponse.from(Page.empty(pageable), summaryMapper());
            }
            categoryId = found.get();
        }

        if (tagSlug != null) {
            Optional<UUID> found = tagTranslationRepository.findByLocaleAndSlug(locale, tagSlug)
                    .map(t -> t.getTag().getId());
            if (found.isEmpty()) {
                return PageResponse.from(Page.empty(pageable), summaryMapper());
            }
            tagId = found.get();
        }

        return PageResponse.from(
                postRepository.search(PostStatus.PUBLISHED, locale, categoryId, tagId, q, pageable),
                summaryMapper());
    }

    @Transactional(readOnly = true)
    public PostDetailResponse findPublishedBySlug(String slug, String locale) {
        Post post = postRepository.findBySlugAndLocaleAndStatus(slug, locale, PostStatus.PUBLISHED)
                .orElseThrow(() -> ResourceNotFoundException.of("error.post.notFound", slug));
        return toPublicDetail(post);
    }

    // ── Admin API ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> findAll(String locale, String q, Pageable pageable) {
        return PageResponse.from(postRepository.search(null, locale, null, null, q, pageable), summaryMapper());
    }

    @Transactional(readOnly = true)
    public PostDetailResponse findById(UUID id) {
        return toDetail(getOrThrow(id));
    }

    @Transactional
    public PostDetailResponse create(CreatePostRequest request, User author) {
        String locale = requireSupportedLocale(request.locale());
        UUID groupId = resolveTranslationGroup(request.translationOf(), locale);

        String baseSlug = SlugUtils.toSlugOrFallback(request.title(), UUID.randomUUID().toString());
        String slug = resolveUniqueSlug(baseSlug, locale);

        Post post = Post.builder()
                .locale(locale)
                .translationGroupId(groupId)
                .title(request.title())
                .slug(slug)
                .excerpt(request.excerpt())
                .content(request.content())
                .status(PostStatus.DRAFT)
                .author(author)
                .category(resolveCategory(request.categoryId()))
                .tags(resolveTags(request.tagIds()))
                .coverMedia(resolveCoverMedia(request.coverMediaId()))
                .build();

        return toDetail(postRepository.save(post));
    }

    @Transactional
    public PostDetailResponse update(UUID id, UpdatePostRequest request, User actor) {
        Post post = getOrThrow(id);
        checkOwnershipForAuthor(post, actor);

        post.setTitle(request.title());
        post.setExcerpt(request.excerpt());
        post.setContent(request.content());
        post.setCategory(resolveCategory(request.categoryId()));
        post.setTags(resolveTags(request.tagIds()));
        post.setCoverMedia(resolveCoverMedia(request.coverMediaId()));

        return toDetail(postRepository.save(post));
    }

    @Transactional
    public PostDetailResponse changeStatus(UUID id, PostStatus newStatus, User actor) {
        Post post = getOrThrow(id);
        boolean isOwner = post.getAuthor().getId().equals(actor.getId());
        PostStatusTransition.validate(post.getStatus(), newStatus, actor.getRole(), isOwner);

        post.setStatus(newStatus);

        if (newStatus == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(Instant.now());
        }

        Post saved = postRepository.save(post);

        if (newStatus == PostStatus.PUBLISHED) {
            eventPublisher.publishEvent(new PostPublishedEvent(saved));
        } else if (newStatus == PostStatus.ARCHIVED) {
            eventPublisher.publishEvent(new PostArchivedEvent(saved));
        }

        return toDetail(saved);
    }

    @Transactional
    public void archive(UUID id, User actor) {
        changeStatus(id, PostStatus.ARCHIVED, actor);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Function<Post, PostSummaryResponse> summaryMapper() {
        return p -> PostSummaryResponse.from(p, categoryLabel(p));
    }

    private PostDetailResponse toDetail(Post post) {
        return PostDetailResponse.from(post,
                postRepository.findByTranslationGroupId(post.getTranslationGroupId()), categoryLabel(post), tagLabels(post));
    }

    private PostDetailResponse toPublicDetail(Post post) {
        return PostDetailResponse.from(post,
                postRepository.findByTranslationGroupIdAndStatus(post.getTranslationGroupId(), PostStatus.PUBLISHED),
                categoryLabel(post), tagLabels(post));
    }

    /** Lokalisierte Tag-Labels in der Sprache des Posts, sonst Default-Locale (ADR-018 Kapitel 9). */
    private List<String> tagLabels(Post post) {
        String def = localeService.getDefaultLocale().toLanguageTag();
        return post.getTags().stream()
                .map(tag -> tagTranslationRepository.findByTag_IdAndLocale(tag.getId(), post.getLocale())
                        .or(() -> tagTranslationRepository.findByTag_IdAndLocale(tag.getId(), def))
                        .map(TagTranslation::getName)
                        .orElse(null))
                .toList();
    }

    /** Lokalisiertes Kategorie-Label in der Sprache des Posts, sonst Default-Locale (ADR-018 Kapitel 9). */
    private String categoryLabel(Post post) {
        if (post.getCategory() == null) {
            return null;
        }
        UUID categoryId = post.getCategory().getId();
        String def = localeService.getDefaultLocale().toLanguageTag();
        return categoryTranslationRepository.findByCategory_IdAndLocale(categoryId, post.getLocale())
                .or(() -> categoryTranslationRepository.findByCategory_IdAndLocale(categoryId, def))
                .map(CategoryTranslation::getName)
                .orElse(null);
    }

    private String requireSupportedLocale(String tag) {
        return localeService.match(tag)
                .map(Locale::toLanguageTag)
                .orElseThrow(() -> BusinessException.of("error.locale.unsupported", tag));
    }

    /** Resolves the translation group and guards against a duplicate locale (ADR-018 Kapitel 10). */
    private UUID resolveTranslationGroup(UUID translationOf, String locale) {
        if (translationOf == null) {
            return UUID.randomUUID();
        }
        UUID groupId = postRepository.findById(translationOf)
                .map(Post::getTranslationGroupId)
                .orElse(translationOf);
        boolean localeTaken = postRepository.findByTranslationGroupId(groupId).stream()
                .anyMatch(p -> p.getLocale().equals(locale));
        if (localeTaken) {
            throw ConflictException.of("error.post.translationExists", locale);
        }
        return groupId;
    }

    private void checkOwnershipForAuthor(Post post, User actor) {
        if (actor.getRole() == Role.AUTHOR && !post.getAuthor().getId().equals(actor.getId())) {
            throw BusinessException.of("error.post.authorOwnership");
        }
    }

    private String resolveUniqueSlug(String base, String locale) {
        String slug = base;
        int counter = 2;
        while (postRepository.existsBySlugAndLocale(slug, locale)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.category.notFound", categoryId));
    }

    private List<Tag> resolveTags(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return new ArrayList<>();
        return tagRepository.findAllById(tagIds);
    }

    private Media resolveCoverMedia(UUID coverMediaId) {
        if (coverMediaId == null) return null;
        return mediaRepository.findById(coverMediaId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.media.notFound", coverMediaId));
    }

    public Post getOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("error.post.notFound", id));
    }
}

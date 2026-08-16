package de.patrick.schmidt.blogcms.post;

import de.patrick.schmidt.blogcms.category.Category;
import de.patrick.schmidt.blogcms.category.CategoryRepository;
import de.patrick.schmidt.blogcms.common.PageResponse;
import de.patrick.schmidt.blogcms.common.SlugUtils;
import de.patrick.schmidt.blogcms.common.exception.BusinessException;
import de.patrick.schmidt.blogcms.common.exception.ResourceNotFoundException;
import de.patrick.schmidt.blogcms.post.dto.*;
import de.patrick.schmidt.blogcms.post.event.PostArchivedEvent;
import de.patrick.schmidt.blogcms.post.event.PostPublishedEvent;
import de.patrick.schmidt.blogcms.tag.Tag;
import de.patrick.schmidt.blogcms.tag.TagRepository;
import de.patrick.schmidt.blogcms.user.Role;
import de.patrick.schmidt.blogcms.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ── Public API ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> findPublished(String categorySlug, String tagSlug, Pageable pageable) {
        if (categorySlug != null) {
            return PageResponse.from(
                    postRepository.findAllByStatusAndCategory_Slug(PostStatus.PUBLISHED, categorySlug, pageable),
                    PostSummaryResponse::from);
        }
        if (tagSlug != null) {
            return PageResponse.from(
                    postRepository.findAllByStatusAndTags_Slug(PostStatus.PUBLISHED, tagSlug, pageable),
                    PostSummaryResponse::from);
        }
        return PageResponse.from(postRepository.findAllByStatus(PostStatus.PUBLISHED, pageable), PostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse findPublishedBySlug(String slug) {
        Post post = postRepository.findBySlugAndStatus(slug, PostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + slug));
        return PostDetailResponse.from(post);
    }

    // ── Admin API ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> findAll(Pageable pageable) {
        return PageResponse.from(postRepository.findAll(pageable), PostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse findById(UUID id) {
        return PostDetailResponse.from(getOrThrow(id));
    }

    @Transactional
    public PostDetailResponse create(CreatePostRequest request, User author) {
        String baseSlug = SlugUtils.toSlugOrFallback(request.title(), UUID.randomUUID().toString());
        String slug = resolveUniqueSlug(baseSlug);

        Post post = Post.builder()
                .title(request.title())
                .slug(slug)
                .excerpt(request.excerpt())
                .content(request.content())
                .status(PostStatus.DRAFT)
                .author(author)
                .category(resolveCategory(request.categoryId()))
                .tags(resolveTags(request.tagIds()))
                .build();

        return PostDetailResponse.from(postRepository.save(post));
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

        return PostDetailResponse.from(postRepository.save(post));
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

        return PostDetailResponse.from(saved);
    }

    @Transactional
    public void archive(UUID id, User actor) {
        changeStatus(id, PostStatus.ARCHIVED, actor);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void checkOwnershipForAuthor(Post post, User actor) {
        if (actor.getRole() == Role.AUTHOR && !post.getAuthor().getId().equals(actor.getId())) {
            throw new BusinessException("Authors can only edit their own posts");
        }
    }

    private String resolveUniqueSlug(String base) {
        String slug = base;
        int counter = 2;
        while (postRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private List<Tag> resolveTags(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return new ArrayList<>();
        return tagRepository.findAllById(tagIds);
    }

    public Post getOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
    }
}

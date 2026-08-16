package de.patrick.schmidt.blogcms.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlugAndLocaleAndStatus(String slug, String locale, PostStatus status);

    Page<Post> findAllByStatusAndLocale(PostStatus status, String locale, Pageable pageable);

    Page<Post> findAllByStatusAndLocaleAndCategory_Slug(PostStatus status, String locale, String categorySlug, Pageable pageable);

    Page<Post> findAllByStatusAndLocaleAndTags_Slug(PostStatus status, String locale, String tagSlug, Pageable pageable);

    List<Post> findByTranslationGroupId(UUID translationGroupId);

    List<Post> findByTranslationGroupIdAndStatus(UUID translationGroupId, PostStatus status);

    boolean existsBySlugAndLocale(String slug, String locale);
}

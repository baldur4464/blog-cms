package de.patrick.schmidt.blogcms.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlugAndStatus(String slug, PostStatus status);

    Optional<Post> findBySlug(String slug);

    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    Page<Post> findAllByStatusAndCategory_Slug(PostStatus status, String categorySlug, Pageable pageable);

    Page<Post> findAllByStatusAndTags_Slug(PostStatus status, String tagSlug, Pageable pageable);

    boolean existsBySlug(String slug);
}

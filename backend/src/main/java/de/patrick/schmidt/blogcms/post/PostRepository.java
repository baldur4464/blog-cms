package de.patrick.schmidt.blogcms.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlugAndLocaleAndStatus(String slug, String locale, PostStatus status);

    List<Post> findByTranslationGroupId(UUID translationGroupId);

    List<Post> findByTranslationGroupIdAndStatus(UUID translationGroupId, PostStatus status);

    boolean existsBySlugAndLocale(String slug, String locale);

    @Query("""
            SELECT p FROM Post p
            WHERE (:status IS NULL OR p.status = :status)
              AND (:locale IS NULL OR p.locale = :locale)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:tagId IS NULL OR EXISTS (
                      SELECT t FROM Tag t WHERE t MEMBER OF p.tags AND t.id = :tagId))
              AND (:q IS NULL OR (
                      LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.excerpt) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.content) LIKE LOWER(CONCAT('%', :q, '%'))))
            """)
    Page<Post> search(
            @Param("status") PostStatus status,
            @Param("locale") String locale,
            @Param("categoryId") UUID categoryId,
            @Param("tagId") UUID tagId,
            @Param("q") String q,
            Pageable pageable);
}

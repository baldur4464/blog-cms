package de.patrick.schmidt.blogcms.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagTranslationRepository extends JpaRepository<TagTranslation, UUID> {

    Optional<TagTranslation> findByTag_IdAndLocale(UUID tagId, String locale);

    List<TagTranslation> findByTag_Id(UUID tagId);

    Optional<TagTranslation> findByLocaleAndSlug(String locale, String slug);

    boolean existsByLocaleAndName(String locale, String name);
}

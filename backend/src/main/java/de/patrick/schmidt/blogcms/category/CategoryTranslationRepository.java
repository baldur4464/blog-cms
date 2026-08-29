package de.patrick.schmidt.blogcms.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, UUID> {

    Optional<CategoryTranslation> findByCategory_IdAndLocale(UUID categoryId, String locale);

    List<CategoryTranslation> findByCategory_Id(UUID categoryId);

    Optional<CategoryTranslation> findByLocaleAndSlug(String locale, String slug);

    boolean existsByLocaleAndName(String locale, String name);
}

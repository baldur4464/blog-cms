package de.patrick.schmidt.blogcms.category;

import de.patrick.schmidt.blogcms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Lokalisierte Felder einer Kategorie (ADR-018 Kapitel 6).
 * Slug ist pro Locale eindeutig (ADR-018 Kapitel 7).
 */
@Entity
@Table(name = "category_translation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTranslation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 35)
    private String locale;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(length = 500)
    private String description;
}

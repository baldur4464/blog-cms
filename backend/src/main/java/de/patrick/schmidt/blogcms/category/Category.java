package de.patrick.schmidt.blogcms.category;

import de.patrick.schmidt.blogcms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Sprachneutrale Kategorie-Identität (ADR-018 Kapitel 6).
 * Übersetzbare Felder (name/slug/description) liegen in {@link CategoryTranslation}.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CategoryTranslation> translations = new ArrayList<>();
}

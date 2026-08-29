package de.patrick.schmidt.blogcms.tag;

import de.patrick.schmidt.blogcms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Sprachneutrale Tag-Identität (ADR-018 Kapitel 6).
 * Übersetzbare Felder (name/slug) liegen in {@link TagTranslation}.
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag extends BaseEntity {

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TagTranslation> translations = new ArrayList<>();
}

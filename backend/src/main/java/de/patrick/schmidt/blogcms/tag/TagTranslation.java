package de.patrick.schmidt.blogcms.tag;

import de.patrick.schmidt.blogcms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Lokalisierte Felder eines Tags (ADR-018 Kapitel 6).
 * Slug ist pro Locale eindeutig (ADR-018 Kapitel 7).
 */
@Entity
@Table(name = "tag_translation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagTranslation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(nullable = false, length = 35)
    private String locale;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;
}

package de.patrick.schmidt.blogcms.user;

import de.patrick.schmidt.blogcms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    /** Persönliche Oberflächensprache (ADR-018 Kapitel 3). Null ⇒ Fallback über Locale-Negotiation. */
    @Column(name = "preferred_locale", length = 35)
    private String preferredLocale;
}

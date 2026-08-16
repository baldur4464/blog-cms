package de.patrick.schmidt.blogcms.media;

import de.patrick.schmidt.blogcms.common.BaseEntity;
import de.patrick.schmidt.blogcms.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 127)
    private String mimeType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, length = 512)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
}

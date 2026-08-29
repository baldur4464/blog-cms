package de.patrick.schmidt.blogcms.post;

import de.patrick.schmidt.blogcms.common.exception.InvalidStatusTransitionException;
import de.patrick.schmidt.blogcms.user.Role;

import java.util.Set;

/**
 * Encodes the allowed status transitions and role-based permissions per ADR-013.
 */
public final class PostStatusTransition {

    private PostStatusTransition() {}

    public static void validate(PostStatus current, PostStatus next, Role actorRole, boolean isOwner) {
        if (current == next) {
            throw InvalidStatusTransitionException.of("error.status.sameStatus", current);
        }

        boolean allowed = switch (current) {
            case DRAFT -> switch (next) {
                case REVIEW -> true;                            // any allowed role (see below)
                case PUBLISHED -> actorRole != Role.AUTHOR;    // EDITOR, ADMIN only
                default -> false;
            };
            case REVIEW -> switch (next) {
                case PUBLISHED, DRAFT -> actorRole != Role.AUTHOR;
                default -> false;
            };
            case PUBLISHED -> switch (next) {
                case ARCHIVED, DRAFT -> actorRole != Role.AUTHOR;
                default -> false;
            };
            case ARCHIVED -> next == PostStatus.DRAFT && actorRole != Role.AUTHOR;
        };

        if (!allowed) {
            throw InvalidStatusTransitionException.of("error.status.notAllowed", current, next, actorRole);
        }

        // AUTHOR may only act on own posts
        if (actorRole == Role.AUTHOR && !isOwner) {
            throw InvalidStatusTransitionException.of("error.status.authorOwnership");
        }
    }
}

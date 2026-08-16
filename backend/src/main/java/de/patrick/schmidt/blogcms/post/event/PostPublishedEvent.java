package de.patrick.schmidt.blogcms.post.event;

import de.patrick.schmidt.blogcms.post.Post;

public record PostPublishedEvent(Post post) {}

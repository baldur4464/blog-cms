package de.patrick.schmidt.blogcms.post;

import de.patrick.schmidt.blogcms.common.PageResponse;
import de.patrick.schmidt.blogcms.post.dto.PostDetailResponse;
import de.patrick.schmidt.blogcms.post.dto.PostSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/v1/posts")
@RequiredArgsConstructor
public class PostPublicController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<PageResponse<PostSummaryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt,desc") String sort,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(postService.findPublished(category, tag, PageRequest.of(page, size, parseSort(sort))));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostDetailResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.findPublishedBySlug(slug));
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, parts[0]);
    }
}

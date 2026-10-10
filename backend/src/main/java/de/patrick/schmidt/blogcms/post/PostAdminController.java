package de.patrick.schmidt.blogcms.post;

import de.patrick.schmidt.blogcms.common.PageResponse;
import de.patrick.schmidt.blogcms.post.dto.*;
import de.patrick.schmidt.blogcms.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/posts")
@RequiredArgsConstructor
public class PostAdminController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<PageResponse<PostSummaryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(postService.findAll(locale, q, PageRequest.of(page, size, parseSort(sort))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PostDetailResponse> create(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(request, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(postService.update(id, request, currentUser));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<PostDetailResponse> changeStatus(
            @PathVariable UUID id,
            @RequestParam PostStatus status,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(postService.changeStatus(id, status, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        postService.archive(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, parts[0]);
    }
}

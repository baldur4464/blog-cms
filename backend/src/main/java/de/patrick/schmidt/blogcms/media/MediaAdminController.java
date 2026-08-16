package de.patrick.schmidt.blogcms.media;

import de.patrick.schmidt.blogcms.common.PageResponse;
import de.patrick.schmidt.blogcms.media.dto.MediaResponse;
import de.patrick.schmidt.blogcms.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/media")
@RequiredArgsConstructor
public class MediaAdminController {

    private final MediaService mediaService;

    @GetMapping
    public ResponseEntity<PageResponse<MediaResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mediaService.findAll(PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<MediaResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.upload(file, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

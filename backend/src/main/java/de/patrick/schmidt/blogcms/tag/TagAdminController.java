package de.patrick.schmidt.blogcms.tag;

import de.patrick.schmidt.blogcms.tag.dto.TagRequest;
import de.patrick.schmidt.blogcms.tag.dto.TagResponse;
import de.patrick.schmidt.blogcms.tag.dto.TagTranslationRequest;
import de.patrick.schmidt.blogcms.tag.dto.TagTranslationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/tags")
@RequiredArgsConstructor
public class TagAdminController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Übersetzungen (ADR-018 Kapitel 10) ────────────────────────────────────

    @GetMapping("/{id}/translations")
    public ResponseEntity<List<TagTranslationResponse>> translations(@PathVariable UUID id) {
        return ResponseEntity.ok(tagService.findTranslations(id));
    }

    @PutMapping("/{id}/translations/{locale}")
    public ResponseEntity<TagTranslationResponse> upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody TagTranslationRequest request) {
        return ResponseEntity.ok(tagService.upsertTranslation(id, locale, request));
    }

    @DeleteMapping("/{id}/translations/{locale}")
    public ResponseEntity<Void> deleteTranslation(@PathVariable UUID id, @PathVariable String locale) {
        tagService.deleteTranslation(id, locale);
        return ResponseEntity.noContent().build();
    }
}

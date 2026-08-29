package de.patrick.schmidt.blogcms.category;

import de.patrick.schmidt.blogcms.category.dto.CategoryRequest;
import de.patrick.schmidt.blogcms.category.dto.CategoryResponse;
import de.patrick.schmidt.blogcms.category.dto.CategoryTranslationRequest;
import de.patrick.schmidt.blogcms.category.dto.CategoryTranslationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Übersetzungen (ADR-018 Kapitel 10) ────────────────────────────────────

    @GetMapping("/{id}/translations")
    public ResponseEntity<List<CategoryTranslationResponse>> translations(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findTranslations(id));
    }

    @PutMapping("/{id}/translations/{locale}")
    public ResponseEntity<CategoryTranslationResponse> upsertTranslation(
            @PathVariable UUID id,
            @PathVariable String locale,
            @Valid @RequestBody CategoryTranslationRequest request) {
        return ResponseEntity.ok(categoryService.upsertTranslation(id, locale, request));
    }

    @DeleteMapping("/{id}/translations/{locale}")
    public ResponseEntity<Void> deleteTranslation(@PathVariable UUID id, @PathVariable String locale) {
        categoryService.deleteTranslation(id, locale);
        return ResponseEntity.noContent().build();
    }
}

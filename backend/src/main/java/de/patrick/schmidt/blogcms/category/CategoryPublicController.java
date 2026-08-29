package de.patrick.schmidt.blogcms.category;

import de.patrick.schmidt.blogcms.category.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/public/v1/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list(Locale locale) {
        return ResponseEntity.ok(categoryService.findAllLocalized(locale.toLanguageTag()));
    }
}

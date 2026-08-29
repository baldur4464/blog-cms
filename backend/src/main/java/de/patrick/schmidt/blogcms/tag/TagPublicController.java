package de.patrick.schmidt.blogcms.tag;

import de.patrick.schmidt.blogcms.tag.dto.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/public/v1/tags")
@RequiredArgsConstructor
public class TagPublicController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponse>> list(Locale locale) {
        return ResponseEntity.ok(tagService.findAllLocalized(locale.toLanguageTag()));
    }
}

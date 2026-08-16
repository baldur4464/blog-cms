package de.patrick.schmidt.blogcms.tag;

import de.patrick.schmidt.blogcms.common.SlugUtils;
import de.patrick.schmidt.blogcms.common.exception.ConflictException;
import de.patrick.schmidt.blogcms.common.exception.ResourceNotFoundException;
import de.patrick.schmidt.blogcms.tag.dto.TagRequest;
import de.patrick.schmidt.blogcms.tag.dto.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> findAll() {
        return tagRepository.findAll().stream().map(TagResponse::from).toList();
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        if (tagRepository.existsByName(request.name())) {
            throw new ConflictException("Tag already exists: " + request.name());
        }
        Tag tag = Tag.builder()
                .name(request.name())
                .slug(SlugUtils.toSlug(request.name()))
                .build();
        return TagResponse.from(tagRepository.save(tag));
    }

    @Transactional
    public TagResponse update(UUID id, TagRequest request) {
        Tag tag = getOrThrow(id);
        tag.setName(request.name());
        tag.setSlug(SlugUtils.toSlug(request.name()));
        return TagResponse.from(tagRepository.save(tag));
    }

    @Transactional
    public void delete(UUID id) {
        getOrThrow(id);
        tagRepository.deleteById(id);
    }

    public Tag getOrThrow(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + id));
    }
}

package de.patrick.schmidt.blogcms.category;

import de.patrick.schmidt.blogcms.category.dto.CategoryRequest;
import de.patrick.schmidt.blogcms.category.dto.CategoryResponse;
import de.patrick.schmidt.blogcms.common.SlugUtils;
import de.patrick.schmidt.blogcms.common.exception.ConflictException;
import de.patrick.schmidt.blogcms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse findById(UUID id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ConflictException("Category already exists: " + request.name());
        }
        Category category = Category.builder()
                .name(request.name())
                .slug(SlugUtils.toSlug(request.name()))
                .description(request.description())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = getOrThrow(id);
        category.setName(request.name());
        category.setSlug(SlugUtils.toSlug(request.name()));
        category.setDescription(request.description());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        getOrThrow(id);
        categoryRepository.deleteById(id);
    }

    public Category getOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}

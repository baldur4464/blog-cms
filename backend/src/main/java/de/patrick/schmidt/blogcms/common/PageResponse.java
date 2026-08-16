package de.patrick.schmidt.blogcms.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <E, D> PageResponse<D> from(Page<E> pageResult, Function<E, D> mapper) {
        return new PageResponse<>(
                pageResult.getContent().stream().map(mapper).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }
}

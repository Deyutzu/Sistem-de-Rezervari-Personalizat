package com.stayfinder.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Răspuns paginat cu format stabil.
 *
 * De ce nu returnăm direct Page<T> din Spring: structura JSON a lui Page
 * conține multe câmpuri interne și nu e garantată stabilă între versiuni.
 * Aplicația Android are nevoie de un contract simplu și previzibil.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    /** Transformă un Page de entități într-un PageResponse de DTO-uri. */
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

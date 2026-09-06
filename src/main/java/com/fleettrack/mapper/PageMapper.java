package com.fleettrack.mapper;

import com.fleettrack.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PageMapper {

    public <E, D> PageResponse<D> toPageResponse(Page<E> page, Function<E, D> mapper) {
        List<D> content = page.getContent().stream().map(mapper).toList();
        return PageResponse.<D>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

package org.aibles.intellihiregrading.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PaginationUtils {

    public static final Set<String> GRADING_SORTABLE_FIELDS = Set.of(
            "createdAt", "lastUpdatedAt"
    );

    private PaginationUtils() {}

    public static Pageable buildPageable(int page, int size, String sort, Set<String> sortableFields) {
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 100) size = 100;

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        if (!sortableFields.contains(sortField)) {
            sortField = "createdAt";
        }

        Sort.Direction direction;
        try {
            direction = sortParams.length > 1
                    ? Sort.Direction.fromString(sortParams[1])
                    : Sort.Direction.DESC;
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}

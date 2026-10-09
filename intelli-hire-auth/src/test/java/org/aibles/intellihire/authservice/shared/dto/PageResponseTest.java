package org.aibles.intellihire.authservice.shared.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    @DisplayName("Should map Spring Data Page to PageResponse correctly")
    void shouldMapFromSpringDataPage() {
        List<String> items = List.of("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(items, PageRequest.of(0, 10), 30);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.getContent()).containsExactlyElementsOf(items);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(30);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }
}

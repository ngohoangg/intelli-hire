package org.aibles.intellihire.authservice.shared.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    private static class ConcreteEntity extends BaseEntity {
        private String name;
    }

    @Test
    @DisplayName("Should populate and read audit fields via builder and setters")
    void shouldBuildAndAccessFields() {
        Instant now = Instant.now();
        ConcreteEntity entity = ConcreteEntity.builder()
                .id("123e4567-e89b-12d3-a456-426614174000")
                .name("test-name")
                .createdAt(now)
                .createdBy("admin")
                .updatedAt(now)
                .updatedBy("admin")
                .build();

        assertThat(entity.getId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(entity.getName()).isEqualTo("test-name");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getCreatedBy()).isEqualTo("admin");
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedBy()).isEqualTo("admin");

        Instant later = now.plusSeconds(60);
        entity.setUpdatedAt(later);
        entity.setUpdatedBy("user-2");
        assertThat(entity.getUpdatedAt()).isEqualTo(later);
        assertThat(entity.getUpdatedBy()).isEqualTo("user-2");
    }
}

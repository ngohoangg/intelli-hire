package org.aibles.intellihire.authservice.shared.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class SampleMapperTest {

    public record SourceDto(String id, String username) {}
    public record TargetDto(String id, String username) {}

    @Mapper
    public interface TestMapper {
        TestMapper INSTANCE = Mappers.getMapper(TestMapper.class);
        TargetDto toTarget(SourceDto source);
    }

    @Test
    @DisplayName("Should verify MapStruct code generation works properly")
    void shouldMapDtoUsingMapStruct() {
        SourceDto source = new SourceDto("uuid-123", "alice");
        TargetDto target = TestMapper.INSTANCE.toTarget(source);

        assertThat(target).isNotNull();
        assertThat(target.id()).isEqualTo("uuid-123");
        assertThat(target.username()).isEqualTo("alice");
    }
}

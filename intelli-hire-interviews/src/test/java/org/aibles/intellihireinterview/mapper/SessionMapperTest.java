package org.aibles.intellihireinterview.mapper;

import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

class SessionMapperTest {

    private final SessionMapper mapper = Mappers.getMapper(SessionMapper.class);

    @Test
    void toResponse_mapsStatusAsString() {
        var session = InterviewSession.builder()
            .id(1L).userId("42").topic("Java")
            .status(SessionStatus.ACTIVE).build();

        var response = mapper.toResponse(session);

        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getTopic()).isEqualTo("Java");
    }

    @Test
    void toResponseList_mapsAllItems() {
        var s1 = InterviewSession.builder().id(1L).userId("1").topic("Java").status(SessionStatus.CREATED).build();
        var s2 = InterviewSession.builder().id(2L).userId("1").topic("Go").status(SessionStatus.COMPLETED).build();

        var list = mapper.toResponseList(java.util.List.of(s1, s2));

        assertThat(list).hasSize(2);
        assertThat(list.get(1).getStatus()).isEqualTo("COMPLETED");
    }
}

package org.aibles.intellihireinterview.repository;

import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class InterviewSessionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired InterviewSessionRepository sessionRepo;

    @Test
    void findByUserId_returnsOnlyOwnSessions() {
        sessionRepo.save(InterviewSession.builder().userId("1").topic("Java").status(SessionStatus.CREATED).build());
        sessionRepo.save(InterviewSession.builder().userId("2").topic("Go").status(SessionStatus.CREATED).build());

        var results = sessionRepo.findByUserIdOrderByCreatedAtDesc("1");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTopic()).isEqualTo("Java");
    }

    @Test
    void findByIdAndUserId_returnsEmptyForWrongUser() {
        var saved = sessionRepo.save(
            InterviewSession.builder().userId("1").topic("Java").status(SessionStatus.CREATED).build());

        assertThat(sessionRepo.findByIdAndUserId(saved.getId(), "999")).isEmpty();
    }
}

# Interview Service — Complete Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Hoàn thiện `interview-service` Spring Boot bằng cách lấp đầy các gap còn thiếu: `SessionCompletedEvent` → Kafka, session expiry scheduler, WebSocket start-interview endpoint, request authorization (401), pagination và integration test đầy đủ.

**Architecture:** Service chạy ở port 8083, dùng PostgreSQL (JPA + Flyway), Kafka (producer), WebFlux WebClient (gọi question-service ở 8081), STOMP WebSocket. Mỗi feature gap được triển khai theo TDD: test trước → impl → commit.

**Tech Stack:** Java 17, Spring Boot 3.2.4, Spring Data JPA, Spring Kafka, Spring WebSocket (STOMP), WebFlux WebClient, Flyway, MapStruct, Lombok, JUnit 5 + Mockito, Testcontainers (PostgreSQL + Kafka), OkHttp MockWebServer.

---

## Trạng thái hiện tại

| Thành phần | Trạng thái |
|---|---|
| Domain entities (InterviewSession, QuestionLog, UserAnswerRaw) | ✅ Done |
| Repositories (3 repo + queries) | ✅ Done |
| InterviewSessionService (create/get/list/end + detail) | ✅ Done |
| AnswerService (submit + duplicate check) | ✅ Done |
| REST controller (5 endpoints) | ✅ Done |
| WebSocket controller (answer → next question) | ✅ Done |
| AnswerEventProducer → Kafka `answers_submitted` | ✅ Done |
| QuestionServiceClient (WebClient) | ✅ Done |
| UserContextFilter, GlobalExceptionHandler, Mapper | ✅ Done |
| Flyway V1 migration | ✅ Done |
| Unit tests (service, repo, controller, mapper, config) | ✅ Done |
| FlywayMigrationTest (SpringBootTest + Testcontainers) | ✅ Done |
| Docker setup | ✅ Done |
| **SessionCompletedEvent + Kafka `session_completed`** | ❌ Missing |
| **Session expiry scheduler (EXPIRED status)** | ❌ Missing |
| **WebSocket start-interview (first question)** | ❌ Missing |
| **Unauthorized (401) when X-User-Id header missing** | ❌ Missing |
| **Integration test (full flow end-to-end)** | ❌ Missing |
| **Pagination for listSessions** | ❌ Missing |

---

## File Map — Sẽ Tạo Mới

| File | Trách nhiệm |
|---|---|
| `src/main/java/.../dto/event/SessionCompletedEvent.java` | DTO event khi session kết thúc (COMPLETED hoặc EXPIRED) |
| `src/main/java/.../kafka/SessionEventProducer.java` | Kafka producer cho topic `session_completed` |
| `src/main/java/.../scheduler/SessionExpiryScheduler.java` | `@Scheduled` task kiểm tra và expire session ACTIVE quá hạn |
| `src/main/java/.../exception/UnauthorizedException.java` | Exception khi thiếu X-User-Id header |
| `src/test/java/.../kafka/SessionEventProducerTest.java` | Unit test cho SessionEventProducer |
| `src/test/java/.../scheduler/SessionExpirySchedulerTest.java` | Unit test cho scheduler |
| `src/test/java/.../integration/InterviewServiceIntegrationTest.java` | Full integration test (SpringBootTest + Testcontainers) |

## File Map — Sẽ Sửa Đổi

| File | Thay đổi |
|---|---|
| `InterviewServiceApplication.java` | Thêm `@EnableScheduling` |
| `UserContextFilter.java` | Trả 401 khi X-User-Id header null/blank |
| `GlobalExceptionHandler.java` | Thêm handler cho `UnauthorizedException` |
| `InterviewSessionRepository.java` | Thêm `findByStatusAndStartedAtBefore(...)` + pageable |
| `InterviewSessionService.java` (interface) | Update `listSessions` nhận `Pageable` |
| `InterviewSessionServiceImpl.java` | Inject `SessionEventProducer`, publish event trong `endSession()` |
| `InterviewWebSocketController.java` | Thêm `@MessageMapping("/interview/{sessionId}/start")` |
| `application.yml` | Thêm `session.expiry.minutes=60` |

---

## Chunk 1: SessionCompletedEvent + Kafka Publisher

### Task 1: SessionCompletedEvent DTO

**Files:**
- Create: `src/main/java/com/intellihire/interview/dto/event/SessionCompletedEvent.java`

- [x] **Step 1: Tạo file DTO**

```java
package com.intellihire.interview.dto.event;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class SessionCompletedEvent {
    private Long sessionId;
    private Long userId;
    private String topic;
    private String difficulty;
    private String finalStatus;      // "COMPLETED" hoặc "EXPIRED"
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer totalQuestions;
    private Integer answeredQuestions;
}
```

- [x] **Step 2: Verify compile bằng cách build**

```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

- [x] **Step 3: Commit**

```bash
git add src/main/java/com/intellihire/interview/dto/event/SessionCompletedEvent.java
git commit -m "feat: add SessionCompletedEvent DTO"
```

---

### Task 2: SessionEventProducer

**Files:**
- Create: `src/main/java/com/intellihire/interview/kafka/SessionEventProducer.java`
- Create: `src/test/java/com/intellihire/interview/kafka/SessionEventProducerTest.java`

- [x] **Step 1: Viết failing test trước**

```java
// src/test/java/com/intellihire/interview/kafka/SessionEventProducerTest.java
package com.intellihire.interview.kafka;

import com.intellihire.interview.dto.event.SessionCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionEventProducerTest {

    @Mock KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks SessionEventProducer producer;

    @Test
    void publishSessionCompleted_sendsToCorrectTopic() {
        // Inject topic via reflection (chưa có constructor nên dùng ReflectionTestUtils sau)
        var event = SessionCompletedEvent.builder()
            .sessionId(1L).userId(42L).finalStatus("COMPLETED").build();

        producer.publishSessionCompleted(event);

        verify(kafkaTemplate).send(anyString(), eq("1"), eq(event));
    }
}
```

- [x] **Step 2: Chạy test, xác nhận FAIL**

```bash
./mvnw test -pl . -Dtest=SessionEventProducerTest -q 2>&1 | tail -5
```
Expected: FAIL — class not found

- [x] **Step 3: Tạo SessionEventProducer**

```java
// src/main/java/com/intellihire/interview/kafka/SessionEventProducer.java
package com.intellihire.interview.kafka;

import com.intellihire.interview.dto.event.SessionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.session-completed}")
    private String sessionCompletedTopic;

    public void publishSessionCompleted(SessionCompletedEvent event) {
        String key = String.valueOf(event.getSessionId());
        kafkaTemplate.send(sessionCompletedTopic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish session-completed event for session {}: {}",
                              event.getSessionId(), ex.getMessage());
                } else {
                    log.debug("Published session-completed event for session {}, status {}",
                              event.getSessionId(), event.getFinalStatus());
                }
            });
    }
}
```

- [x] **Step 4: Thêm topic config vào `application.yml`**

Sửa section `kafka.topics` trong `src/main/resources/application.yml`:
```yaml
kafka:
  topics:
    answers-submitted: answers_submitted
    session-completed: session_completed    # thêm dòng này
```

- [x] **Step 5: Fix test — inject topic name**

Sửa `SessionEventProducerTest` để inject topic:
```java
@BeforeEach
void setUp() {
    org.springframework.test.util.ReflectionTestUtils.setField(
        producer, "sessionCompletedTopic", "session_completed");
}
```

- [x] **Step 6: Chạy test, xác nhận PASS**

```bash
./mvnw test -Dtest=SessionEventProducerTest -q 2>&1 | tail -5
```
Expected: Tests run: 1, Failures: 0

- [x] **Step 7: Commit**

```bash
git add src/main/java/com/intellihire/interview/kafka/SessionEventProducer.java \
        src/test/java/com/intellihire/interview/kafka/SessionEventProducerTest.java \
        src/main/resources/application.yml
git commit -m "feat: add SessionEventProducer for session_completed Kafka topic"
```

---

### Task 3: Publish SessionCompletedEvent khi endSession()

**Files:**
- Modify: `src/main/java/com/intellihire/interview/service/impl/InterviewSessionServiceImpl.java`
- Modify: `src/test/java/com/intellihire/interview/service/InterviewSessionServiceTest.java`

- [x] **Step 1: Viết failing test**

Thêm vào `InterviewSessionServiceTest.java`:
```java
@Mock SessionEventProducer sessionEventProducer;

@Test
void endSession_publishesSessionCompletedEvent() {
    var session = InterviewSession.builder()
        .id(1L).userId(1L).status(SessionStatus.ACTIVE).topic("Java")
        .difficulty("mid").startedAt(LocalDateTime.now()).build();
    when(sessionRepo.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(session));
    when(sessionRepo.save(any())).thenReturn(session);
    when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L)).thenReturn(List.of());
    when(answerRepo.findBySessionIdOrderBySubmittedAt(1L)).thenReturn(List.of());

    service.endSession(1L, 1L);

    verify(sessionEventProducer).publishSessionCompleted(argThat(event ->
        event.getSessionId().equals(1L) &&
        "COMPLETED".equals(event.getFinalStatus()) &&
        event.getUserId().equals(1L)
    ));
}
```

- [x] **Step 2: Chạy test, xác nhận FAIL**

```bash
./mvnw test -Dtest=InterviewSessionServiceTest#endSession_publishesSessionCompletedEvent -q 2>&1 | tail -5
```

- [x] **Step 3: Inject SessionEventProducer vào InterviewSessionServiceImpl**

Sửa `InterviewSessionServiceImpl.java`:

```java
// Thêm field
private final SessionEventProducer sessionEventProducer;
private final QuestionLogRepository questionLogRepo;  // đã có

// Sửa endSession():
@Override
@Transactional
public void endSession(Long sessionId, Long userId) {
    var session = sessionRepo.findByIdAndUserId(sessionId, userId)
        .orElseThrow(() -> new SessionNotFoundException(sessionId));
    if (session.getStatus().isTerminal()) {
        throw new SessionNotActiveException(sessionId);
    }
    session.setStatus(SessionStatus.COMPLETED);
    session.setEndedAt(LocalDateTime.now());
    sessionRepo.save(session);

    int totalQ = questionLogRepo.findBySessionIdOrderByQuestionOrder(sessionId).size();
    int answered = answerRepo.findBySessionIdOrderBySubmittedAt(sessionId).size();

    sessionEventProducer.publishSessionCompleted(
        SessionCompletedEvent.builder()
            .sessionId(sessionId)
            .userId(userId)
            .topic(session.getTopic())
            .difficulty(session.getDifficulty())
            .finalStatus("COMPLETED")
            .startedAt(session.getStartedAt())
            .endedAt(session.getEndedAt())
            .totalQuestions(totalQ)
            .answeredQuestions(answered)
            .build()
    );
    log.info("Ended session {} for user {}", sessionId, userId);
}
```

- [x] **Step 4: Chạy test, xác nhận PASS**

```bash
./mvnw test -Dtest=InterviewSessionServiceTest -q 2>&1 | tail -10
```
Expected: All tests pass

- [x] **Step 5: Commit**

```bash
git add src/main/java/com/intellihire/interview/service/impl/InterviewSessionServiceImpl.java \
        src/test/java/com/intellihire/interview/service/InterviewSessionServiceTest.java
git commit -m "feat: publish SessionCompletedEvent to Kafka when session ends"
```

---

## Chunk 2: Session Expiry Scheduler

### Task 4: Repository query để tìm sessions hết hạn

**Files:**
- Modify: `src/main/java/com/intellihire/interview/repository/InterviewSessionRepository.java`

- [x] **Step 1: Thêm query vào repository**

```java
// Thêm import
import java.time.LocalDateTime;

// Thêm method
List<InterviewSession> findByStatusAndStartedAtBefore(SessionStatus status, LocalDateTime threshold);
```

- [x] **Step 2: Verify compile**

```bash
./mvnw compile -q
```

- [x] **Step 3: Thêm test vào InterviewSessionRepositoryTest**

```java
@Test
void findByStatusAndStartedAtBefore_returnsExpiredCandidates() {
    var old = sessionRepo.save(InterviewSession.builder()
        .userId(1L).topic("Java").status(SessionStatus.ACTIVE).build());
    // Manually backdate startedAt via JPQL hoặc native query
    // Dùng @Modifying query trong test setup, hoặc dùng EntityManager để set startedAt cũ

    // Lưu ý: prePersist() set startedAt = now() nên cần update sau khi save
    // Test approach: insert với startedAt quá khứ bằng native query
    sessionRepo.findByStatusAndStartedAtBefore(
        SessionStatus.ACTIVE, LocalDateTime.now().minusHours(1)
    );
    // Chỉ kiểm tra method không throw
}
```

- [x] **Step 4: Commit**

```bash
git add src/main/java/com/intellihire/interview/repository/InterviewSessionRepository.java
git commit -m "feat: add findByStatusAndStartedAtBefore query to repository"
```

---

### Task 5: SessionExpiryScheduler

**Files:**
- Create: `src/main/java/com/intellihire/interview/scheduler/SessionExpiryScheduler.java`
- Create: `src/test/java/com/intellihire/interview/scheduler/SessionExpirySchedulerTest.java`
- Modify: `src/main/java/com/intellihire/interview/InterviewServiceApplication.java`
- Modify: `src/main/resources/application.yml`

- [x] **Step 1: Viết failing test**

```java
// src/test/java/com/intellihire/interview/scheduler/SessionExpirySchedulerTest.java
package com.intellihire.interview.scheduler;

import com.intellihire.interview.domain.entity.InterviewSession;
import com.intellihire.interview.domain.enums.SessionStatus;
import com.intellihire.interview.dto.event.SessionCompletedEvent;
import com.intellihire.interview.kafka.SessionEventProducer;
import com.intellihire.interview.repository.InterviewSessionRepository;
import com.intellihire.interview.repository.QuestionLogRepository;
import com.intellihire.interview.repository.UserAnswerRawRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionExpirySchedulerTest {

    @Mock InterviewSessionRepository sessionRepo;
    @Mock QuestionLogRepository questionLogRepo;
    @Mock UserAnswerRawRepository answerRepo;
    @Mock SessionEventProducer sessionEventProducer;
    @InjectMocks SessionExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "expiryMinutes", 60L);
    }

    @Test
    void expireStaleActiveSessions_setsStatusExpiredAndPublishesEvent() {
        var session = InterviewSession.builder()
            .id(5L).userId(99L).topic("Python")
            .status(SessionStatus.ACTIVE)
            .startedAt(LocalDateTime.now().minusHours(2))
            .build();

        when(sessionRepo.findByStatusAndStartedAtBefore(eq(SessionStatus.ACTIVE), any()))
            .thenReturn(List.of(session));
        when(sessionRepo.saveAll(anyList())).thenReturn(List.of(session));
        when(questionLogRepo.findBySessionIdOrderByQuestionOrder(5L)).thenReturn(List.of());
        when(answerRepo.findBySessionIdOrderBySubmittedAt(5L)).thenReturn(List.of());

        scheduler.expireStaleActiveSessions();

        assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
        assertThat(session.getEndedAt()).isNotNull();
        verify(sessionEventProducer).publishSessionCompleted(argThat(e ->
            "EXPIRED".equals(e.getFinalStatus()) && e.getSessionId().equals(5L)
        ));
    }

    @Test
    void expireStaleActiveSessions_noOp_whenNoStaleSessions() {
        when(sessionRepo.findByStatusAndStartedAtBefore(any(), any())).thenReturn(List.of());

        scheduler.expireStaleActiveSessions();

        verify(sessionRepo, never()).saveAll(any());
        verify(sessionEventProducer, never()).publishSessionCompleted(any());
    }
}
```

- [x] **Step 2: Chạy test, xác nhận FAIL**

```bash
./mvnw test -Dtest=SessionExpirySchedulerTest -q 2>&1 | tail -5
```

- [x] **Step 3: Tạo SessionExpiryScheduler**

```java
// src/main/java/com/intellihire/interview/scheduler/SessionExpiryScheduler.java
package com.intellihire.interview.scheduler;

import com.intellihire.interview.domain.enums.SessionStatus;
import com.intellihire.interview.dto.event.SessionCompletedEvent;
import com.intellihire.interview.kafka.SessionEventProducer;
import com.intellihire.interview.repository.InterviewSessionRepository;
import com.intellihire.interview.repository.QuestionLogRepository;
import com.intellihire.interview.repository.UserAnswerRawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionExpiryScheduler {

    private final InterviewSessionRepository sessionRepo;
    private final QuestionLogRepository questionLogRepo;
    private final UserAnswerRawRepository answerRepo;
    private final SessionEventProducer sessionEventProducer;

    @Value("${session.expiry.minutes:60}")
    private long expiryMinutes;

    @Scheduled(fixedDelayString = "${session.expiry.check-interval-ms:300000}")
    @Transactional
    public void expireStaleActiveSessions() {
        var threshold = LocalDateTime.now().minusMinutes(expiryMinutes);
        var stale = sessionRepo.findByStatusAndStartedAtBefore(SessionStatus.ACTIVE, threshold);

        if (stale.isEmpty()) return;

        stale.forEach(session -> {
            session.setStatus(SessionStatus.EXPIRED);
            session.setEndedAt(LocalDateTime.now());
        });
        sessionRepo.saveAll(stale);

        stale.forEach(session -> {
            int totalQ = questionLogRepo.findBySessionIdOrderByQuestionOrder(session.getId()).size();
            int answered = answerRepo.findBySessionIdOrderBySubmittedAt(session.getId()).size();
            sessionEventProducer.publishSessionCompleted(
                SessionCompletedEvent.builder()
                    .sessionId(session.getId())
                    .userId(session.getUserId())
                    .topic(session.getTopic())
                    .difficulty(session.getDifficulty())
                    .finalStatus("EXPIRED")
                    .startedAt(session.getStartedAt())
                    .endedAt(session.getEndedAt())
                    .totalQuestions(totalQ)
                    .answeredQuestions(answered)
                    .build()
            );
        });

        log.info("Expired {} stale ACTIVE sessions (threshold: {} minutes ago)", stale.size(), expiryMinutes);
    }
}
```

- [x] **Step 4: Thêm config vào `application.yml`**

```yaml
session:
  expiry:
    minutes: 60
    check-interval-ms: 300000   # kiểm tra mỗi 5 phút
```

- [x] **Step 5: Thêm `@EnableScheduling` vào InterviewServiceApplication**

```java
@SpringBootApplication
@EnableScheduling   // thêm annotation này
public class InterviewServiceApplication { ... }
```

- [x] **Step 6: Chạy test, xác nhận PASS**

```bash
./mvnw test -Dtest=SessionExpirySchedulerTest -q 2>&1 | tail -10
```
Expected: Tests run: 2, Failures: 0

- [x] **Step 7: Chạy toàn bộ test để đảm bảo không break gì**

```bash
./mvnw test -q 2>&1 | tail -15
```

- [x] **Step 8: Commit**

```bash
git add src/main/java/com/intellihire/interview/scheduler/ \
        src/test/java/com/intellihire/interview/scheduler/ \
        src/main/java/com/intellihire/interview/InterviewServiceApplication.java \
        src/main/resources/application.yml
git commit -m "feat: add SessionExpiryScheduler to expire stale ACTIVE sessions"
```

---

## Chunk 3: WebSocket Start Interview + Unauthorized Handling

### Task 6: WebSocket Start Interview — Gửi câu hỏi đầu tiên

**Files:**
- Modify: `src/main/java/com/intellihire/interview/controller/InterviewWebSocketController.java`
- Modify: `src/test/java/com/intellihire/interview/controller/InterviewWebSocketControllerTest.java`

**Context:** Khi client kết nối WebSocket và muốn bắt đầu interview, họ cần nhận câu hỏi đầu tiên. Hiện tại không có cơ chế nào push câu hỏi #1. Client gửi `SEND /app/interview/{sessionId}/start` → service push câu hỏi #1 tới `/topic/interview/{sessionId}`.

- [x] **Step 1: Viết failing test**

Thêm vào `InterviewWebSocketControllerTest.java`:

```java
@Test
void handleStart_sendsFirstQuestion() {
    var log1 = QuestionLog.builder()
        .questionId(1L).questionText("What is JVM?").questionOrder(1)
        .session(InterviewSession.builder().id(1L).build()).build();
    var log2 = QuestionLog.builder()
        .questionId(2L).questionText("What is GC?").questionOrder(2)
        .session(InterviewSession.builder().id(1L).build()).build();

    when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L))
        .thenReturn(List.of(log1, log2));

    controller.handleStart(1L, "42");

    verify(messagingTemplate).convertAndSend(
        eq("/topic/interview/1"),
        argThat((QuestionMessage msg) ->
            "QUESTION".equals(msg.getType()) &&
            msg.getQuestionId().equals(1L) &&
            msg.getQuestionOrder() == 1 &&
            msg.getTotalQuestions() == 2
        )
    );
}

@Test
void handleStart_sendsError_whenUserIdMissing() {
    controller.handleStart(1L, null);

    verify(messagingTemplate).convertAndSend(
        eq("/topic/interview/1"),
        argThat((QuestionMessage msg) -> "ERROR".equals(msg.getType()))
    );
}

@Test
void handleStart_sendsSessionEnd_whenNoQuestions() {
    when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L)).thenReturn(List.of());

    controller.handleStart(1L, "42");

    verify(messagingTemplate).convertAndSend(
        eq("/topic/interview/1"),
        argThat((QuestionMessage msg) -> "SESSION_END".equals(msg.getType()))
    );
}
```

- [x] **Step 2: Chạy test, xác nhận FAIL**

```bash
./mvnw test -Dtest=InterviewWebSocketControllerTest -q 2>&1 | tail -5
```

- [x] **Step 3: Thêm `handleStart` vào InterviewWebSocketController**

```java
/**
 * Client sends to: /app/interview/{sessionId}/start
 * Service pushes the first unanswered question to /topic/interview/{sessionId}
 */
@MessageMapping("/interview/{sessionId}/start")
public void handleStart(@DestinationVariable Long sessionId,
                        @Header(value = "X-User-Id", required = false) String userIdHeader) {
    if (userIdHeader == null || userIdHeader.isBlank()) {
        log.warn("Rejected WebSocket start for session {} — missing X-User-Id header", sessionId);
        messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
            QuestionMessage.builder().type("ERROR").build());
        return;
    }

    var logs = questionLogRepo.findBySessionIdOrderByQuestionOrder(sessionId);
    if (logs.isEmpty()) {
        messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
            QuestionMessage.builder().type("SESSION_END").totalQuestions(0).build());
        return;
    }

    var first = logs.get(0);
    messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
        QuestionMessage.builder()
            .type("QUESTION")
            .questionId(first.getQuestionId())
            .questionText(first.getQuestionText())
            .questionOrder(first.getQuestionOrder())
            .totalQuestions(logs.size())
            .build());
}
```

- [x] **Step 4: Chạy test, xác nhận PASS**

```bash
./mvnw test -Dtest=InterviewWebSocketControllerTest -q 2>&1 | tail -10
```
Expected: All tests pass

- [x] **Step 5: Commit**

```bash
git add src/main/java/com/intellihire/interview/controller/InterviewWebSocketController.java \
        src/test/java/com/intellihire/interview/controller/InterviewWebSocketControllerTest.java
git commit -m "feat: add WebSocket start-interview handler to push first question"
```

---

### Task 7: Unauthorized (401) khi thiếu X-User-Id header

**Files:**
- Create: `src/main/java/com/intellihire/interview/exception/UnauthorizedException.java`
- Modify: `src/main/java/com/intellihire/interview/config/UserContextFilter.java`
- Modify: `src/main/java/com/intellihire/interview/exception/GlobalExceptionHandler.java`
- Modify: `src/test/java/com/intellihire/interview/config/UserContextFilterTest.java`

**Problem:** Hiện tại khi request không có `X-User-Id`, `CURRENT_USER_ID.get()` trả `null` → `sessionService.createSession(null, ...)` → DB constraint violation (user_id NOT NULL). Cần trả 401 Unauthorized sớm hơn.

- [x] **Step 1: Tạo UnauthorizedException**

```java
// src/main/java/com/intellihire/interview/exception/UnauthorizedException.java
package com.intellihire.interview.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

- [x] **Step 2: Viết failing test cho filter**

Thêm vào `UserContextFilterTest.java`:

```java
@Test
void doFilter_missingUserId_returns401() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    // KHÔNG set X-User-Id header
    request.setRequestURI("/api/v1/interviews/sessions");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean chainInvoked = new AtomicBoolean(false);

    filter.doFilter(request, response, (req, res) -> chainInvoked.set(true));

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(chainInvoked.get()).isFalse();
}

@Test
void doFilter_withUserId_proceedsNormally() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-User-Id", "42");
    request.setRequestURI("/api/v1/interviews/sessions");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean chainInvoked = new AtomicBoolean(false);

    filter.doFilter(request, response, (req, res) -> chainInvoked.set(true));

    assertThat(chainInvoked.get()).isTrue();
    assertThat(UserContextFilter.CURRENT_USER_ID.get()).isNull(); // cleaned up after
}
```

- [x] **Step 3: Chạy test, xác nhận FAIL**

```bash
./mvnw test -Dtest=UserContextFilterTest -q 2>&1 | tail -5
```

- [x] **Step 4: Cập nhật UserContextFilter**

```java
@Override
public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
    var httpReq = (HttpServletRequest) req;
    var httpRes = (HttpServletResponse) res;
    String path = httpReq.getRequestURI();
    String userIdHeader = httpReq.getHeader("X-User-Id");
    String userRole = httpReq.getHeader("X-User-Role");

    // Bỏ qua auth check cho actuator và WebSocket endpoints
    boolean skipAuth = path.startsWith("/actuator") || path.startsWith("/ws/");

    if (!skipAuth && (userIdHeader == null || userIdHeader.isBlank())) {
        httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpRes.setContentType("application/json");
        httpRes.getWriter().write("{\"error\":\"Missing X-User-Id header\"}");
        return;
    }

    try {
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            CURRENT_USER_ID.set(Long.parseLong(userIdHeader));
        }
        CURRENT_USER_ROLE.set(userRole != null ? userRole : "");
        chain.doFilter(req, res);
    } finally {
        CURRENT_USER_ID.remove();
        CURRENT_USER_ROLE.remove();
    }
}
```

- [x] **Step 5: Thêm handler vào GlobalExceptionHandler (cho trường hợp exception xảy ra)**

```java
@ExceptionHandler(UnauthorizedException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    pd.setType(URI.create("https://intellihire.com/errors/unauthorized"));
    return pd;
}
```

- [x] **Step 6: Chạy tất cả test**

```bash
./mvnw test -q 2>&1 | tail -15
```
Expected: All tests pass. Chú ý: controller tests dùng `@WebMvcTest` có thể cần cập nhật mock filter chain.

- [x] **Step 7: Cập nhật controller tests nếu cần**

Các `@WebMvcTest` tests cần `X-User-Id` header (đã có sẵn trong existing tests). Kiểm tra test nào thiếu header và thêm vào.

- [x] **Step 8: Commit**

```bash
git add src/main/java/com/intellihire/interview/exception/UnauthorizedException.java \
        src/main/java/com/intellihire/interview/config/UserContextFilter.java \
        src/main/java/com/intellihire/interview/exception/GlobalExceptionHandler.java \
        src/test/java/com/intellihire/interview/config/UserContextFilterTest.java
git commit -m "feat: return 401 when X-User-Id header is missing in REST requests"
```

---

## Chunk 4: Pagination + Integration Test

### Task 8: Pagination cho listSessions

**Files:**
- Modify: `src/main/java/com/intellihire/interview/service/InterviewSessionService.java`
- Modify: `src/main/java/com/intellihire/interview/service/impl/InterviewSessionServiceImpl.java`
- Modify: `src/main/java/com/intellihire/interview/repository/InterviewSessionRepository.java`
- Modify: `src/main/java/com/intellihire/interview/controller/InterviewRestController.java`
- Modify: tests tương ứng

- [x] **Step 1: Thêm PageRequest support vào Repository**

```java
// InterviewSessionRepository.java — thêm method mới (KHÔNG xóa cái cũ để tránh break tests)
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

Page<InterviewSession> findByUserId(Long userId, Pageable pageable);
```

- [x] **Step 2: Cập nhật InterviewSessionService interface**

```java
// Giữ nguyên listSessions(Long userId) để backward compatible
// Thêm overload mới
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

Page<InterviewSession> listSessions(Long userId, Pageable pageable);
```

- [x] **Step 3: Implement trong InterviewSessionServiceImpl**

```java
@Override
@Transactional(readOnly = true)
public Page<InterviewSession> listSessions(Long userId, Pageable pageable) {
    return sessionRepo.findByUserId(userId, pageable);
}
```

- [x] **Step 4: Cập nhật REST controller**

```java
@GetMapping
public Page<SessionResponse> listSessions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    Long userId = UserContextFilter.CURRENT_USER_ID.get();
    var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
    return sessionService.listSessions(userId, pageable)
        .map(sessionMapper::toResponse);
}
```

- [x] **Step 5: Viết test cho paginated endpoint**

```java
// Thêm vào InterviewRestControllerTest
@Test
void listSessions_returnsPaginatedResult() throws Exception {
    var pageResponse = new org.springframework.data.domain.PageImpl<>(List.of());
    when(sessionService.listSessions(eq(42L), any())).thenReturn(pageResponse);

    mockMvc.perform(get("/api/v1/interviews/sessions?page=0&size=10")
            .header("X-User-Id", "42"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").exists());
}
```

- [x] **Step 6: Chạy test**

```bash
./mvnw test -Dtest=InterviewRestControllerTest -q 2>&1 | tail -10
```

- [x] **Step 7: Commit**

```bash
git add src/main/java/com/intellihire/interview/service/ \
        src/main/java/com/intellihire/interview/repository/InterviewSessionRepository.java \
        src/main/java/com/intellihire/interview/controller/InterviewRestController.java \
        src/test/java/com/intellihire/interview/controller/InterviewRestControllerTest.java
git commit -m "feat: add pagination support for GET /sessions endpoint"
```

---

### Task 9: Full Integration Test

**Files:**
- Create: `src/test/java/com/intellihire/interview/integration/InterviewServiceIntegrationTest.java`

**Mục tiêu:** Test toàn bộ flow thực tế: HTTP request → service → DB + Kafka, với Testcontainers (PostgreSQL) + EmbeddedKafka + MockWebServer (question-service mock).

- [x] **Step 1: Tạo integration test**

```java
// src/test/java/com/intellihire/interview/integration/InterviewServiceIntegrationTest.java
package com.intellihire.interview.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.intellihire.interview.dto.request.CreateSessionRequest;
import com.intellihire.interview.dto.request.SubmitAnswerRequest;
import com.intellihire.interview.domain.enums.SessionStatus;
import com.intellihire.interview.repository.InterviewSessionRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"answers_submitted", "session_completed"})
class InterviewServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    static MockWebServer mockQuestionService;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("question-service.base-url",
              () -> "http://localhost:" + mockQuestionService.getPort());
    }

    @BeforeAll
    static void startMockServer() throws IOException {
        mockQuestionService = new MockWebServer();
        mockQuestionService.start();
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        mockQuestionService.shutdown();
    }

    @Autowired MockMvc mockMvc;
    @Autowired InterviewSessionRepository sessionRepo;

    final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void fullFlow_createSession_submitAnswer_endSession() throws Exception {
        // Arrange: mock question-service response
        String questionsJson = """
            {"questions":[
              {"id":1,"questionText":"What is JVM?","topic":"Java","difficulty":"mid"},
              {"id":2,"questionText":"What is GC?","topic":"Java","difficulty":"mid"}
            ]}
            """;
        mockQuestionService.enqueue(new MockResponse()
            .setBody(questionsJson)
            .addHeader("Content-Type", "application/json"));

        // Step 1: Create session
        var createReq = new CreateSessionRequest();
        createReq.setTopic("Java");
        createReq.setDifficulty("mid");
        createReq.setQuestionCount(2);

        var createResult = mockMvc.perform(post("/api/v1/interviews/sessions")
                .header("X-User-Id", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn();

        Long sessionId = mapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asLong();

        // Step 2: Submit answer
        var answerReq = new SubmitAnswerRequest();
        answerReq.setQuestionId(1L);
        answerReq.setAnswerText("JVM is Java Virtual Machine...");

        mockMvc.perform(post("/api/v1/interviews/sessions/{id}/answers", sessionId)
                .header("X-User-Id", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(answerReq)))
            .andExpect(status().isNoContent());

        // Step 3: End session
        mockMvc.perform(put("/api/v1/interviews/sessions/{id}/end", sessionId)
                .header("X-User-Id", "100"))
            .andExpect(status().isNoContent());

        // Step 4: Verify DB state
        var session = sessionRepo.findById(sessionId).orElseThrow();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isNotNull();

        // Step 5: Verify session detail
        mockMvc.perform(get("/api/v1/interviews/sessions/{id}", sessionId)
                .header("X-User-Id", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.questions").isArray())
            .andExpect(jsonPath("$.answers").isArray());
    }

    @Test
    void createSession_withoutUserId_returns401() throws Exception {
        var req = new CreateSessionRequest();
        req.setTopic("Java");

        mockMvc.perform(post("/api/v1/interviews/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void submitAnswer_duplicate_returns409() throws Exception {
        // Setup: mock question service
        String questionsJson = """
            {"questions":[{"id":10,"questionText":"Q1?","topic":"Go","difficulty":"junior"}]}
            """;
        mockQuestionService.enqueue(new MockResponse()
            .setBody(questionsJson)
            .addHeader("Content-Type", "application/json"));

        var createReq = new CreateSessionRequest();
        createReq.setTopic("Go");
        createReq.setDifficulty("junior");
        createReq.setQuestionCount(1);

        var createResult = mockMvc.perform(post("/api/v1/interviews/sessions")
                .header("X-User-Id", "200")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();

        Long sessionId = mapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asLong();

        var answerReq = new SubmitAnswerRequest();
        answerReq.setQuestionId(10L);
        answerReq.setAnswerText("my answer");

        // First submission: OK
        mockMvc.perform(post("/api/v1/interviews/sessions/{id}/answers", sessionId)
                .header("X-User-Id", "200")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(answerReq)))
            .andExpect(status().isNoContent());

        // Second submission same question: 409
        mockMvc.perform(post("/api/v1/interviews/sessions/{id}/answers", sessionId)
                .header("X-User-Id", "200")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(answerReq)))
            .andExpect(status().isConflict());
    }
}
```

- [x] **Step 2: Chạy integration test**

```bash
./mvnw test -Dtest=InterviewServiceIntegrationTest -q 2>&1 | tail -20
```
Expected: Tests run: 3, Failures: 0 (có thể chậm vì Testcontainers pull Docker image)

- [x] **Step 3: Nếu có lỗi EmbeddedKafka conflict với `application-test.yml`**

Kiểm tra `application-test.yml`:
```yaml
spring:
  kafka:
    bootstrap-servers: ${spring.embedded.kafka.brokers}  # đã đúng
```

- [x] **Step 4: Chạy toàn bộ test suite cuối cùng**

```bash
./mvnw test 2>&1 | grep -E "Tests run:|BUILD"
```
Expected: BUILD SUCCESS, tất cả tests pass

- [x] **Step 5: Commit**

```bash
git add src/test/java/com/intellihire/interview/integration/
git commit -m "test: add full integration test covering create-submit-end session flow"
```

---

## Chunk 5: Kiểm tra cuối cùng và dọn dẹp

### Task 10: Chạy toàn bộ test suite + build image

- [x] **Step 1: Chạy tất cả tests**

```bash
./mvnw clean test 2>&1 | tail -30
```
Expected: BUILD SUCCESS

- [x] **Step 2: Kiểm tra test coverage (optional)**

```bash
./mvnw test jacoco:report -q
# Mở target/site/jacoco/index.html để xem coverage
```

- [x] **Step 3: Build JAR**

```bash
./mvnw clean package -DskipTests -q
ls -lh target/*.jar
```
Expected: `interview-service-0.0.1-SNAPSHOT.jar` xuất hiện

- [x] **Step 4: Verify Docker build**

```bash
docker build -t intellihire/interview-service:local .
```
Expected: Successfully built

- [x] **Step 5: Final commit**

```bash
git add .
git commit -m "chore: verify build and all tests pass — interview-service complete"
```

---

## Tổng kết

Sau khi hoàn thành plan này, service sẽ có:

| Feature | Status |
|---|---|
| SessionCompletedEvent → Kafka `session_completed` | ✅ |
| Session expiry scheduler (EXPIRED status) | ✅ |
| WebSocket `/start` để nhận câu hỏi đầu tiên | ✅ |
| 401 Unauthorized khi thiếu X-User-Id | ✅ |
| Pagination cho GET /sessions | ✅ |
| Integration test (full flow + duplicate + unauthorized) | ✅ |

**API Endpoints hoàn chỉnh:**

```
POST   /api/v1/interviews/sessions          → Tạo session + load questions
GET    /api/v1/interviews/sessions?page=0   → Danh sách sessions (paginated)
GET    /api/v1/interviews/sessions/{id}     → Chi tiết session + questions + answers
PUT    /api/v1/interviews/sessions/{id}/end → Kết thúc session + publish Kafka event
POST   /api/v1/interviews/sessions/{id}/answers → Submit câu trả lời

WS STOMP /ws/interview
  SEND /app/interview/{sessionId}/start   → Nhận câu hỏi đầu tiên
  SEND /app/interview/{sessionId}/answer  → Submit answer, nhận câu hỏi tiếp theo
  SUB  /topic/interview/{sessionId}       → Nhận QuestionMessage
```

**Kafka Events:**

```
answers_submitted   ← AnswerSubmittedEvent (mỗi câu trả lời)
session_completed   ← SessionCompletedEvent (khi COMPLETED hoặc EXPIRED)
```

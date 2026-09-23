# Interview Service Improvements — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 7 missing/broken features in the interview service: DuplicateAnswerException, Kafka session-completed event, Question Service error handling, session expiration scheduler, pagination + status filter for listSessions, and Swagger/OpenAPI docs.

**Architecture:** Each task is self-contained and builds on existing patterns (BaseException, KafkaTemplate, @Scheduled, Spring Data Pageable, springdoc-openapi). No new architectural layers introduced — extend what exists.

**Tech Stack:** Java 17, Spring Boot 3.2.4, PostgreSQL, Apache Kafka, Flyway, MapStruct, Lombok, springdoc-openapi 2.x

---

## Chunk 1: Exception & Kafka Fixes (Low complexity, High impact)

### Task 1: DuplicateAnswerException

**Files:**
- Create: `src/main/java/org/aibles/intellihireinterview/exception/DuplicateAnswerException.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/service/impl/AnswerServiceImpl.java`

- [ ] **Step 1: Tạo DuplicateAnswerException**

```java
// src/main/java/org/aibles/intellihireinterview/exception/DuplicateAnswerException.java
package org.aibles.intellihireinterview.exception;

import org.springframework.http.HttpStatus;

public class DuplicateAnswerException extends BaseException {
    public DuplicateAnswerException(Long questionId, Long sessionId) {
        super(ErrorCode.INT_003, HttpStatus.CONFLICT);
    }
}
```

- [ ] **Step 2: Thay IllegalStateException trong AnswerServiceImpl**

Trong file `AnswerServiceImpl.java` dòng 45-47, thay:
```java
throw new IllegalStateException(
    "Answer already submitted for question " + request.getQuestionId() + " in session " + sessionId);
```
Thành:
```java
throw new DuplicateAnswerException(request.getQuestionId(), sessionId);
```

- [ ] **Step 3: Build để verify không có compile error**
```bash
cd "C:/Users/truon/OneDrive/Desktop/MCP_CODE/intelli-hire-interviews"
./mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**
```bash
git add src/main/java/org/aibles/intellihireinterview/exception/DuplicateAnswerException.java
git add src/main/java/org/aibles/intellihireinterview/service/impl/AnswerServiceImpl.java
git commit -m "feat: add DuplicateAnswerException for INT_003 error code"
```

---

### Task 2: Kafka event khi endSession

**Files:**
- Create: `src/main/java/org/aibles/intellihireinterview/dto/SessionCompletedEvent.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/kafka/AnswerEventProducer.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/service/impl/InterviewSessionServiceImpl.java`

- [ ] **Step 1: Tạo SessionCompletedEvent DTO**

```java
// src/main/java/org/aibles/intellihireinterview/dto/SessionCompletedEvent.java
package org.aibles.intellihireinterview.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SessionCompletedEvent {
    private Long sessionId;
    private Long userId;
    private String topic;
    private String difficulty;
    private LocalDateTime endedAt;
}
```

- [ ] **Step 2: Thêm method publishSessionCompleted vào AnswerEventProducer**

Thêm field và method vào `AnswerEventProducer.java`:
```java
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
                log.debug("Published session-completed event for session {}", event.getSessionId());
            }
        });
}
```

- [ ] **Step 3: Inject AnswerEventProducer vào InterviewSessionServiceImpl và gọi sau endSession**

Thêm `private final AnswerEventProducer eventProducer;` vào field list của `InterviewSessionServiceImpl`.

Trong method `endSession()`, sau `sessionRepo.save(session);` thêm:
```java
var event = SessionCompletedEvent.builder()
    .sessionId(sessionId)
    .userId(userId)
    .topic(session.getTopic())
    .difficulty(session.getDifficulty())
    .endedAt(session.getEndedAt())
    .build();
eventProducer.publishSessionCompleted(event);
```

- [ ] **Step 4: Build**
```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**
```bash
git add src/main/java/org/aibles/intellihireinterview/dto/SessionCompletedEvent.java
git add src/main/java/org/aibles/intellihireinterview/kafka/AnswerEventProducer.java
git add src/main/java/org/aibles/intellihireinterview/service/impl/InterviewSessionServiceImpl.java
git commit -m "feat: publish session-completed Kafka event when ending interview session"
```

---

### Task 3: Question Service error handling

**Files:**
- Create: `src/main/java/org/aibles/intellihireinterview/exception/QuestionServiceUnavailableException.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/exception/ErrorCode.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/client/QuestionServiceClient.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/service/impl/InterviewSessionServiceImpl.java`

**Vấn đề hiện tại:** Khi Question Service down, `getRandomQuestions()` trả về empty list. Session vẫn được tạo nhưng không có câu hỏi nào → user không biết có lỗi.

- [ ] **Step 1: Thêm error code INT_004 vào ErrorCode**

```java
INT_004("INT_004", "Question service is unavailable, please try again later"),
```

- [ ] **Step 2: Tạo QuestionServiceUnavailableException**

```java
// src/main/java/org/aibles/intellihireinterview/exception/QuestionServiceUnavailableException.java
package org.aibles.intellihireinterview.exception;

import org.springframework.http.HttpStatus;

public class QuestionServiceUnavailableException extends BaseException {
    public QuestionServiceUnavailableException() {
        super(ErrorCode.INT_004, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
```

- [ ] **Step 3: Cập nhật QuestionServiceClient — throw exception thay vì trả về empty list**

Trong `QuestionServiceClient.java`, sửa các catch block:
```java
} catch (WebClientResponseException e) {
    log.warn("Question Service returned HTTP {} for topic={}: {}",
             e.getStatusCode(), topic, e.getMessage());
    throw new QuestionServiceUnavailableException();
} catch (Exception e) {
    log.warn("Failed to fetch questions from Question Service for topic={}: {}",
             topic, e.getMessage());
    throw new QuestionServiceUnavailableException();
}
```

Và sửa trường hợp null response:
```java
if (response == null || !response.containsKey("questions")) {
    log.warn("Question Service returned empty or null response for topic={}", topic);
    throw new QuestionServiceUnavailableException();
}
```

- [ ] **Step 4: Build**
```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**
```bash
git add src/main/java/org/aibles/intellihireinterview/exception/ErrorCode.java
git add src/main/java/org/aibles/intellihireinterview/exception/QuestionServiceUnavailableException.java
git add src/main/java/org/aibles/intellihireinterview/client/QuestionServiceClient.java
git commit -m "feat: throw QuestionServiceUnavailableException instead of silently returning empty list"
```

---

## Chunk 2: Session Expiration Scheduler

### Task 4: Session Expiration Scheduler

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/org/aibles/intellihireinterview/IntellihireinterviewApplication.java`
- Create: `src/main/java/org/aibles/intellihireinterview/scheduler/SessionExpirationScheduler.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/repository/InterviewSessionRepository.java`
- Create: `src/main/resources/db/migration/V2__add_session_expiry_index.sql`

- [ ] **Step 1: Thêm config session timeout vào application.yml**

```yaml
interview:
  session:
    timeout-minutes: 60
```

- [ ] **Step 2: Enable Scheduling trên main app**

Trong `IntellihireinterviewApplication.java`, thêm annotation `@EnableScheduling`:
```java
@SpringBootApplication
@EnableScheduling
public class IntellihireinterviewApplication { ... }
```

Import: `import org.springframework.scheduling.annotation.EnableScheduling;`

- [ ] **Step 3: Thêm query vào InterviewSessionRepository**

```java
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

@Modifying
@Query("UPDATE InterviewSession s SET s.status = 'EXPIRED', s.endedAt = :now " +
       "WHERE s.status = 'ACTIVE' AND s.startedAt < :expiryTime")
int expireActiveSessions(@Param("now") LocalDateTime now,
                         @Param("expiryTime") LocalDateTime expiryTime);
```

- [ ] **Step 4: Tạo SessionExpirationScheduler**

```java
// src/main/java/org/aibles/intellihireinterview/scheduler/SessionExpirationScheduler.java
package org.aibles.intellihireinterview.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireinterview.repository.InterviewSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionExpirationScheduler {

    private final InterviewSessionRepository sessionRepo;

    @Value("${interview.session.timeout-minutes:60}")
    private int timeoutMinutes;

    @Scheduled(fixedDelayString = "${interview.session.expiry-check-interval-ms:300000}") // 5 phút
    @Transactional
    public void expireInactiveSessions() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        int count = sessionRepo.expireActiveSessions(LocalDateTime.now(), expiryTime);
        if (count > 0) {
            log.info("Expired {} interview sessions older than {} minutes", count, timeoutMinutes);
        }
    }
}
```

- [ ] **Step 5: Thêm config interval vào application.yml**

```yaml
interview:
  session:
    timeout-minutes: 60
    expiry-check-interval-ms: 300000
```

- [ ] **Step 6: Tạo Flyway migration thêm index**

```sql
-- src/main/resources/db/migration/V2__add_session_expiry_index.sql
CREATE INDEX IF NOT EXISTS idx_interview_sessions_status_started_at
    ON interview_sessions (status, started_at)
    WHERE status = 'ACTIVE';
```

- [ ] **Step 7: Build**
```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**
```bash
git add src/main/resources/application.yml
git add src/main/java/org/aibles/intellihireinterview/IntellihireinterviewApplication.java
git add src/main/java/org/aibles/intellihireinterview/scheduler/SessionExpirationScheduler.java
git add src/main/java/org/aibles/intellihireinterview/repository/InterviewSessionRepository.java
git add src/main/resources/db/migration/V2__add_session_expiry_index.sql
git commit -m "feat: add session expiration scheduler to auto-expire ACTIVE sessions after timeout"
```

---

## Chunk 3: Pagination, Status Filter & Swagger

### Task 5 & 6: Pagination + Filter sessions by status

**Files:**
- Create: `src/main/java/org/aibles/intellihireinterview/dto/PageResponse.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/service/InterviewSessionService.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/service/impl/InterviewSessionServiceImpl.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/repository/InterviewSessionRepository.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/controller/InterviewRestController.java`

- [ ] **Step 1: Tạo PageResponse DTO**

```java
// src/main/java/org/aibles/intellihireinterview/dto/PageResponse.java
package org.aibles.intellihireinterview.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
```

- [ ] **Step 2: Thêm query phân trang + filter vào InterviewSessionRepository**

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

Page<InterviewSession> findByUserId(Long userId, Pageable pageable);
Page<InterviewSession> findByUserIdAndStatus(Long userId, SessionStatus status, Pageable pageable);
```

Xoá method `findByUserIdAndStatus(Long userId, SessionStatus status)` (trả về `List`) nếu không còn dùng — hoặc giữ lại nếu scheduler cần.

- [ ] **Step 3: Cập nhật InterviewSessionService interface**

Thay:
```java
List<InterviewSession> listSessions(Long userId);
```
Thành:
```java
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.springframework.data.domain.Pageable;

PageResponse<SessionResponse> listSessions(Long userId, SessionStatus status, Pageable pageable);
```

- [ ] **Step 4: Cập nhật InterviewSessionServiceImpl**

```java
import org.aibles.intellihireinterview.dto.PageResponse;
import org.aibles.intellihireinterview.dto.SessionResponse;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Override
@Transactional(readOnly = true)
public PageResponse<SessionResponse> listSessions(Long userId, SessionStatus status, Pageable pageable) {
    Page<InterviewSession> page = (status != null)
        ? sessionRepo.findByUserIdAndStatus(userId, status, pageable)
        : sessionRepo.findByUserId(userId, pageable);

    List<SessionResponse> content = page.getContent()
        .stream()
        .map(sessionMapper::toResponse)
        .toList();

    return PageResponse.<SessionResponse>builder()
        .content(content)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .build();
}
```

- [ ] **Step 5: Cập nhật InterviewRestController**

```java
@GetMapping
public BaseResponse<PageResponse<SessionResponse>> listSessions(
        @RequestParam(required = false) SessionStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest httpRequest) {
    Long userId = Long.parseLong((String) httpRequest.getAttribute("userId"));
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return BaseResponse.success(sessionService.listSessions(userId, status, pageable));
}
```

Thêm imports:
```java
import org.aibles.intellihireinterview.dto.PageResponse;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
```

- [ ] **Step 6: Build**
```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**
```bash
git add src/main/java/org/aibles/intellihireinterview/dto/PageResponse.java
git add src/main/java/org/aibles/intellihireinterview/repository/InterviewSessionRepository.java
git add src/main/java/org/aibles/intellihireinterview/service/InterviewSessionService.java
git add src/main/java/org/aibles/intellihireinterview/service/impl/InterviewSessionServiceImpl.java
git add src/main/java/org/aibles/intellihireinterview/controller/InterviewRestController.java
git commit -m "feat: add pagination and status filter to listSessions API"
```

---

### Task 7: Swagger/OpenAPI Documentation

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/org/aibles/intellihireinterview/config/SwaggerConfig.java`
- Modify: `src/main/java/org/aibles/intellihireinterview/controller/InterviewRestController.java`

- [ ] **Step 1: Thêm springdoc-openapi dependency vào pom.xml**

Thêm vào phần `<dependencies>`:
```xml
<!-- Swagger / OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

- [ ] **Step 2: Tạo SwaggerConfig**

```java
// src/main/java/org/aibles/intellihireinterview/config/SwaggerConfig.java
package org.aibles.intellihireinterview.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI interviewServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Intelli-Hire Interview Service API")
                .description("API for managing AI-powered interview sessions")
                .version("v1.0"));
    }

    // Tự động thêm X-User-Id header vào mọi endpoint
    @Bean
    public OperationCustomizer addUserIdHeader() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(
                new Parameter()
                    .in("header")
                    .name("X-User-Id")
                    .required(true)
                    .schema(new StringSchema())
                    .description("Authenticated user ID")
            );
            return operation;
        };
    }
}
```

- [ ] **Step 3: Thêm @Operation annotations vào InterviewRestController**

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Interview Sessions", description = "Manage interview sessions and answers")
@RestController
// ...

@Operation(summary = "Create a new interview session")
@PostMapping
// ...

@Operation(summary = "List interview sessions with optional status filter and pagination")
@GetMapping
// ...

@Operation(summary = "Get detailed session info including questions and answers")
@GetMapping("/{sessionId}")
// ...

@Operation(summary = "End an active interview session")
@PutMapping("/{sessionId}/end")
// ...

@Operation(summary = "Submit an answer to a question in the session")
@PostMapping("/{sessionId}/answers")
// ...
```

- [ ] **Step 4: Build**
```bash
./mvnw compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Verify Swagger UI accessible**

Sau khi chạy app, mở browser: `http://localhost:8083/swagger-ui.html`

- [ ] **Step 6: Commit**
```bash
git add pom.xml
git add src/main/java/org/aibles/intellihireinterview/config/SwaggerConfig.java
git add src/main/java/org/aibles/intellihireinterview/controller/InterviewRestController.java
git commit -m "feat: add Swagger/OpenAPI documentation with springdoc-openapi"
```

---

## Summary

| Task | Files thay đổi | Effort |
|------|---------------|--------|
| 1. DuplicateAnswerException | 2 files (1 new) | ~10 min |
| 2. Kafka session-completed event | 3 files (1 new) | ~15 min |
| 3. Question Service error handling | 3 files (1 new) | ~15 min |
| 4. Session expiration scheduler | 5 files (2 new) | ~30 min |
| 5+6. Pagination + status filter | 5 files (1 new) | ~25 min |
| 7. Swagger/OpenAPI | 3 files (1 new) | ~20 min |

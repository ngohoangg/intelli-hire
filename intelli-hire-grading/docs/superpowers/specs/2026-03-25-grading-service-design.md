# IntelliHire Grading Service — Design Document

**Version:** 1.1
**Date:** 2026-03-25
**Service:** intelli-hire-grading
**Port:** 8084

---

## 1. Overview

Grading Service chịu trách nhiệm chấm điểm câu trả lời phỏng vấn bằng AI (LLM). Nhận batch câu trả lời từ Interview Service qua REST API, gọi LLM song song để chấm điểm từng câu, lưu kết quả, và tự động gọi Report Service tạo báo cáo.

### Decisions

| Decision | Choice |
|----------|--------|
| Input | REST API (no Kafka) |
| LLM Provider | OpenRouter (OpenAI-compatible) |
| Grading flow | Batch sync + parallel LLM calls (CompletableFuture) |
| Report creation | Grading auto-calls Report Service |
| Database | Separate `grading_db` (PostgreSQL) |
| Port | 8084 |
| Pattern | Follow Report Service patterns exactly |

---

## 2. Tech Stack

- Java 17 + Spring Boot 3.2.1
- PostgreSQL 16 (Flyway migrations)
- Redis 7 (caching)
- WebClient (OpenRouter API + Report Service client)
- CompletableFuture (parallel LLM calls)
- Lombok, springdoc-openapi, Actuator

### Additional Dependencies (vs Report Service)

- `spring-boot-starter-webflux` — WebClient for OpenRouter API

---

## 3. Project Structure

```
intelli-hire-grading/
├── src/main/java/org/aibles/intellihiregrading/
│   ├── IntelliHireGradingApplication.java
│   ├── config/
│   │   ├── JpaAuditingConfig.java
│   │   ├── OpenApiConfig.java
│   │   ├── RedisConfig.java
│   │   ├── OpenRouterConfig.java
│   │   └── AsyncConfig.java
│   ├── controller/
│   │   └── GradingController.java
│   ├── service/
│   │   ├── GradingService.java
│   │   ├── LlmService.java
│   │   ├── ReportClientService.java
│   │   └── impl/
│   │       ├── GradingServiceImpl.java
│   │       ├── OpenRouterLlmServiceImpl.java
│   │       └── ReportClientServiceImpl.java
│   ├── repository/
│   │   ├── GradingSessionRepository.java
│   │   └── GradingResultRepository.java
│   ├── entity/
│   │   ├── BaseEntity.java
│   │   ├── GradingSession.java
│   │   ├── GradingResult.java
│   │   └── enums/
│   │       └── GradingStatus.java
│   ├── dto/
│   │   ├── BaseResponse.java
│   │   ├── PageResponse.java
│   │   ├── FieldError.java
│   │   ├── GradeRequest.java
│   │   ├── AnswerRequest.java
│   │   ├── GradingResponse.java
│   │   ├── GradingResultResponse.java
│   │   ├── GradingSummaryResponse.java
│   │   └── openrouter/
│   │       ├── ChatRequest.java
│   │       ├── ChatResponse.java
│   │       └── LlmGradingResult.java
│   ├── mapper/
│   │   └── GradingMapper.java
│   ├── exception/
│   │   ├── BaseException.java
│   │   ├── ErrorCode.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── NotFoundException.java
│   │   ├── BadRequestException.java
│   │   ├── DuplicateException.java
│   │   └── ForbiddenException.java
│   └── utils/
│       ├── PaginationUtils.java
│       ├── SecurityUtils.java
│       └── PromptBuilder.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__init.sql
├── src/test/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── .env
```

---

## 4. Database Schema (`grading_db`)

```sql
CREATE TABLE IF NOT EXISTS grading_sessions (
    id              VARCHAR(36) PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    topic           VARCHAR(255),
    status          VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    total_questions INTEGER DEFAULT 0,
    graded_count    INTEGER DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      VARCHAR(255),
    last_updated_at TIMESTAMP DEFAULT NOW(),
    last_updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS grading_results (
    id                 VARCHAR(36) PRIMARY KEY,
    grading_session_id VARCHAR(36) NOT NULL REFERENCES grading_sessions(id) ON DELETE CASCADE,
    question_id        VARCHAR(36) NOT NULL,
    question_text      TEXT,
    answer_text        TEXT,
    skill_name         VARCHAR(255),
    score              DOUBLE PRECISION DEFAULT 0,
    max_score          DOUBLE PRECISION DEFAULT 10,
    feedback           TEXT,
    prompt_used        TEXT,
    model_used         VARCHAR(100),
    latency_ms         BIGINT,
    created_at         TIMESTAMP DEFAULT NOW(),
    created_by         VARCHAR(255),
    last_updated_at    TIMESTAMP DEFAULT NOW(),
    last_updated_by    VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_grading_sessions_session_id ON grading_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_grading_sessions_user_id ON grading_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_grading_sessions_status ON grading_sessions(status);
CREATE INDEX IF NOT EXISTS idx_grading_results_session_id ON grading_results(grading_session_id);
CREATE INDEX IF NOT EXISTS idx_grading_results_question_id ON grading_results(question_id);
```

### GradingStatus Enum

| Status | Description |
|--------|-------------|
| PROCESSING | Batch grading in progress |
| COMPLETED | All questions graded successfully |
| FAILED | Grading failed (LLM error) |

---

## 5. Entities

### BaseEntity (copy from Report Service)
- UUID primary key, audit fields (createdAt, createdBy, lastUpdatedAt, lastUpdatedBy)

### GradingSession
- `sessionId` (String, UNIQUE) — interview session ID
- `userId` (Long)
- `topic` (String)
- `status` (GradingStatus) — PROCESSING/COMPLETED/FAILED
- `totalQuestions` (Integer)
- `gradedCount` (Integer)
- `errorMessage` (String)

### GradingResult
- `gradingSessionId` (String, plain column FK — consistent with Report Service's `QuestionScore.reportId`)
- `questionId` (String)
- `questionText` (String)
- `answerText` (String)
- `skillName` (String)
- `score` (Double, 0-10)
- `maxScore` (Double, default 10)
- `feedback` (String) — LLM detailed feedback
- `promptUsed` (String) — audit
- `modelUsed` (String) — e.g. "openai/gpt-4o"
- `latencyMs` (Long) — LLM response time in ms

**Note:** `GradingSession` does NOT store `totalScore`/`maxScore` fields. These are computed on-the-fly from `GradingResult` rows when building `GradingResponse` (same pattern as Report Service computes from `QuestionScore`).

---

## 6. DTOs

### Request

**GradeRequest:**
- `sessionId` (String, required)
- `userId` (Long, required)
- `topic` (String)
- `answers` (List\<AnswerRequest\>, required, non-empty)

**AnswerRequest:**
- `questionId` (String, required)
- `questionText` (String)
- `answerText` (String)
- `skillName` (String)
- `maxScore` (Double, 1-100, default 10)

### Response

**GradingResponse:**
- `id`, `sessionId`, `userId`, `topic`, `status`
- `totalQuestions`, `totalScore`, `maxScore`
- `results` (List\<GradingResultResponse\>)
- `createdAt`

**GradingResultResponse:**
- `id`, `questionId`, `questionText`, `answerText`
- `skillName`, `score`, `maxScore`, `feedback`
- `modelUsed`, `latencyMs`, `createdAt`

**GradingSummaryResponse** (lightweight for lists):
- `id`, `sessionId`, `userId`, `topic`, `status`
- `totalQuestions`, `createdAt`

### LLM DTOs (under `dto/openrouter/`)

**LlmGradingResult** — internal result from LLM parsing:
- `score` (Double) — graded score
- `feedback` (String) — detailed feedback text
- `model` (String) — model used e.g. "openai/gpt-4o"

**ChatRequest** — OpenRouter API request:
- `model` (String)
- `messages` (List\<Message\>) — [{role, content}]
- `temperature` (Double)
- `maxTokens` (Integer)

**ChatResponse** — OpenRouter API response:
- `choices` (List\<Choice\>) — [{message: {content}}]
- `usage` (Usage) — {promptTokens, completionTokens, totalTokens}
- `model` (String)

### Shared DTOs (copy from Report)
- `BaseResponse<T>`, `PageResponse<T>`, `FieldError`

All DTOs use `@JsonNaming(SnakeCaseStrategy)`.

---

## 7. API Endpoints

Controller mapping: `@RequestMapping("/v1/gradings")`. The `/api` prefix comes from `server.servlet.context-path=/api` in application.yml (same as Report Service).

### Core

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/v1/gradings` | Grade batch answers for a session | 201 |
| GET | `/v1/gradings/{id}` | Get grading by ID | 200 |
| GET | `/v1/gradings/session/{sessionId}` | Get grading by session ID | 200 |

### History & Admin

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/v1/gradings/history/{userId}` | User grading history (paginated, filter by status) | 200 |
| GET | `/v1/gradings/admin/all` | Admin get all gradings (X-User-Role check) | 200 |

No DELETE/PUT — grading results are immutable.

### Sample Request/Response

**POST /api/v1/gradings**

Request:
```json
{
  "session_id": "sess-abc-123",
  "user_id": 42,
  "topic": "Java Spring Boot",
  "answers": [
    {
      "question_id": "q-001",
      "question_text": "Explain dependency injection in Spring",
      "answer_text": "DI is a design pattern where Spring container injects dependencies...",
      "skill_name": "spring-core",
      "max_score": 10
    },
    {
      "question_id": "q-002",
      "question_text": "What is the difference between @Component and @Bean?",
      "answer_text": "@Component is class-level annotation for auto-scanning...",
      "skill_name": "spring-core",
      "max_score": 10
    }
  ]
}
```

Response (201):
```json
{
  "success": true,
  "data": {
    "id": "grd-uuid-456",
    "session_id": "sess-abc-123",
    "user_id": 42,
    "topic": "Java Spring Boot",
    "status": "COMPLETED",
    "total_questions": 2,
    "total_score": 15.5,
    "max_score": 20.0,
    "results": [
      {
        "id": "res-uuid-1",
        "question_id": "q-001",
        "question_text": "Explain dependency injection in Spring",
        "answer_text": "DI is a design pattern where Spring container injects dependencies...",
        "skill_name": "spring-core",
        "score": 8.5,
        "max_score": 10.0,
        "feedback": "Good explanation of DI concept. Could mention constructor vs setter injection.",
        "model_used": "openai/gpt-4o",
        "latency_ms": 2340,
        "created_at": "2026-03-25T10:30:00"
      },
      {
        "id": "res-uuid-2",
        "question_id": "q-002",
        "question_text": "What is the difference between @Component and @Bean?",
        "answer_text": "@Component is class-level annotation for auto-scanning...",
        "skill_name": "spring-core",
        "score": 7.0,
        "max_score": 10.0,
        "feedback": "Correct but incomplete. Missing that @Bean is method-level in @Configuration classes.",
        "model_used": "openai/gpt-4o",
        "latency_ms": 1890,
        "created_at": "2026-03-25T10:30:00"
      }
    ],
    "created_at": "2026-03-25T10:30:00"
  },
  "timestamp": "2026-03-25T10:30:05"
}
```

---

## 8. Service Layer

### GradingService Interface
```
grade(GradeRequest) → GradingResponse
getById(String id) → GradingResponse
getBySessionId(String sessionId) → GradingResponse
getHistoryByUserId(Long userId, String status, Pageable) → PageResponse<GradingSummaryResponse>
getAllGradings(Pageable) → PageResponse<GradingSummaryResponse>
```

### GradingServiceImpl — grade() Flow

**Transactional boundaries:** LLM calls (step 3) happen OUTSIDE `@Transactional` to avoid holding DB connections for 30+ seconds. Only DB writes (steps 2, 5, 6) are wrapped in transactions.

```
1. Validate + check duplicate sessionId
2. @Transactional: Save GradingSession (status = PROCESSING)
3. NO transaction: Parallel LLM calls via CompletableFuture (thread pool)
   - Each future uses .handle() to catch individual errors
   - Failed questions: score = 0, feedback = "Unable to grade"
4. Wait all complete (CompletableFuture.join)
5. @Transactional: Save all GradingResults + update session (COMPLETED/FAILED)
6. NO transaction: Call Report Service POST /api/v1/reports
7. Return GradingResponse
```

**Idempotency:** If a session exists with status FAILED, allow re-grading (delete old results, re-process). Only reject duplicates for PROCESSING or COMPLETED sessions.

### LlmService Interface
```
grade(String prompt) → LlmGradingResult {score, feedback, model}
```

### OpenRouterLlmServiceImpl
- WebClient calls `https://openrouter.ai/api/v1/chat/completions`
- **JSON parsing robustness:** Strip markdown code fences (```json ... ```), trim whitespace, handle common LLM quirks before JSON parsing
- Config: model, temperature, API key, timeout via application.yml
- 1 retry with 2s backoff on transient failures (timeout, 5xx)

### ReportClientService Interface
```
createReport(GradingSession, List<GradingResult>) → void
```

### ReportClientServiceImpl
- WebClient calls `POST http://report-service:8085/api/v1/reports`
- Maps to Report Service's `CreateReportRequest`:
  - `sessionId` → from GradingSession.sessionId
  - `userId` → from GradingSession.userId
  - `topic` → from GradingSession.topic
  - `summary` → null (Report Service can generate later)
  - `roadmapJson` → null (Report Service can generate later)
  - `questionScores` → List of QuestionScoreRequest mapped from GradingResult:
    - `questionId`, `questionText`, `answerText`, `skillName`
    - `score`, `maxScore`, `feedback`
- On failure: log error, do NOT throw (grading result still returned)

### PromptBuilder (util)
Builds standardized prompt:
```
You are a technical interviewer. Grade the following answer.
Topic: {topic}
Skill: {skillName}
Question: {questionText}
Answer: {answerText}
Max Score: {maxScore}

Respond in JSON format:
{"score": <number>, "feedback": "<detailed feedback>"}
```

---

## 9. Configuration

### application.yml Key Sections
- Database: PostgreSQL (`intelli_hire_grading`), Hikari pool
- Redis: caching with TTLs
- Server: port 8084, context-path /api
- OpenRouter: api-key, base-url, model, temperature, max-tokens, timeout
- Report Service: base-url
- Thread pool: core-size 10, max-size 20, queue-capacity 100
- Flyway, JPA validate, Actuator, Swagger

### Redis Cache Names
- `grading` (1h TTL) — by grading ID, `@Cacheable` on getById
- `grading_session` (1h TTL) — by session ID, `@Cacheable` on getBySessionId
- No eviction needed on `grade()` since it creates new entries
- `RedisConfig` implements `CachingConfigurer` (consistent with Report Service)

### AsyncConfig
- `@EnableAsync`
- `TaskExecutor` bean: core-size 10, max-size 20, queue-capacity 100
- **Note:** Sized to handle 2 concurrent batch gradings of 10 questions each

### Known Limitations (v1)
- Synchronous blocking: caller waits for all LLM calls (~3-5s for 10 questions)
- No rate limiting on OpenRouter calls — acceptable for v1 load
- Thread pool caps concurrent LLM calls — may queue under heavy load

---

## 10. Error Handling

### Error Codes
| Code | Message |
|------|---------|
| GRD_001 | Grading session not found |
| GRD_002 | Grading already exists for session |
| GRD_003 | Invalid session id |
| GRD_004 | Invalid grading status |
| GRD_005 | Access denied, admin role required |
| GRD_006 | LLM service unavailable |
| GRD_007 | LLM response parse error |
| GRD_008 | Report service unavailable |
| COM_001 | Validation error |
| COM_002 | Internal server error |
| COM_003 | Bad request |

### LLM Error Strategy
- Timeout (30s): throw GRD_006
- Parse error: fallback score = 0, feedback = "Unable to grade", log warning, do NOT fail batch
- Partial failure: save results for successful questions, failed questions get score = 0

### Report Service Error Strategy
- Call fails: log error, do NOT throw — grading results still returned to client

---

## 11. End-to-End Flow

```
Interview Service
    │
    │ POST /api/v1/gradings
    │ {sessionId, userId, topic, answers: [...]}
    │
    ▼
Grading Service
    │
    ├─ 1. Validate + check duplicate
    ├─ 2. Save GradingSession (PROCESSING)
    ├─ 3. Parallel LLM calls (CompletableFuture x N)
    │      ├─ Q1 → OpenRouter → {score, feedback}
    │      ├─ Q2 → OpenRouter → {score, feedback}
    │      └─ QN → OpenRouter → {score, feedback}
    ├─ 4. Save GradingResults + update session (COMPLETED)
    ├─ 5. Call Report Service POST /api/v1/reports
    │      └─ Report Service creates report + user progress
    └─ 6. Return GradingResponse
```

---

## 12. Docker & Infrastructure

### Dockerfile (multi-stage, same as Report)
- Build: maven:3.9-eclipse-temurin-17
- Runtime: eclipse-temurin:17-jre-jammy
- Expose: 8084

### docker-compose.yml
- PostgreSQL 16-alpine (intelli_hire_grading, port 5433)
- Redis 7-alpine (port 6380)

### .env
```
DATABASE_URL=jdbc:postgresql://localhost:5433/intelli_hire_grading
SERVER_PORT=8084
OPENROUTER_API_KEY=sk-or-...
OPENROUTER_MODEL=openai/gpt-4o
REPORT_SERVICE_URL=http://localhost:8085/api
```

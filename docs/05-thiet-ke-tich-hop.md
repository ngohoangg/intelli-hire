# 05 – Thiết kế tích hợp

Chuỗi nghiệp vụ chính được thiết kế chạy qua 2 sự kiện Kafka (`session_completed`, `grading_completed`), 1 kênh WebSocket và 4 lời gọi REST liên service.

## 5.1 Luồng end-to-end

```mermaid
sequenceDiagram
    autonumber
    actor U as Candidate
    participant FE as Angular
    participant INT as interview-service
    participant QUE as question-service
    participant K as Kafka
    participant GRA as grading-service
    participant LLM as OpenRouter
    participant REP as report-service

    U->>FE: Chọn topic, độ khó, số câu
    FE->>INT: POST /api/v1/interviews/sessions
    INT->>QUE: GET /api/v1/questions/random?count=N
    QUE-->>INT: danh sách câu hỏi
    INT-->>FE: 201 session (ACTIVE)
    FE->>INT: STOMP CONNECT /ws/interview (JWT)
    FE->>INT: SEND /app/interview/{id}/start
    INT-->>FE: /user/queue/interview QUESTION #1
    loop mỗi câu
        U->>FE: nhập câu trả lời
        FE->>INT: SEND /app/interview/{id}/answer
        INT-->>FE: QUESTION kế tiếp
    end
    INT-->>FE: SESSION_END
    INT->>K: session_completed (COMPLETED)
    K->>GRA: session_completed
    GRA->>INT: GET /api/v1/interviews/sessions/{id}
    GRA->>QUE: GET câu hỏi + rubric
    par mỗi câu trả lời
        GRA->>LLM: POST /chat/completions
        LLM-->>GRA: {"score","feedback"}
    end
    GRA->>K: grading_completed
    K->>REP: grading_completed
    REP->>REP: lưu report, skill score, cập nhật progress
    loop polling 3s (≤ 40 lần)
        FE->>GRA: GET /api/v1/gradings/session/{id}
    end
    FE->>REP: GET /api/v1/reports/session/{id}
```

## 5.2 Danh mục sự kiện Kafka

| Topic | Producer | Consumer (group) | Key | Khi nào phát |
|---|---|---|---|---|
| `session_completed` | interviews (`SessionEventProducer`) | grading (`grading-service`) | sessionId | Phiên kết thúc (thủ công, hết câu, hết hạn) |
| `grading_completed` | grading (`GradingCompletedProducer`) | report (`report-service`) | sessionId | Chấm xong một phiên |

Cấu hình chung: `JsonSerializer`/`JsonDeserializer`, **không** type header, consumer `ignoreUnknown`, `auto-offset-reset=earliest`. Tên topic cấu hình qua `kafka.topics.*` (grading/report nhận override từ `KAFKA_TOPIC_*`).

### 5.2.1 `session_completed`

```json
{
  "sessionId": 42,
  "userId": "7",
  "topic": "Spring Boot",
  "difficulty": "MEDIUM",
  "finalStatus": "COMPLETED",
  "startedAt": "2026-09-29T10:00:00",
  "endedAt": "2026-09-29T10:20:00",
  "totalQuestions": 5,
  "answeredQuestions": 5
}
```
`finalStatus` ∈ `COMPLETED` | `EXPIRED`. Consumer xử lý: 3 retry cách 5 s (`FixedBackOff(5000, 3)`), sau đó chuyển sang `session_completed.DLT`.

### 5.2.2 `grading_completed`

```json
{
  "sessionId": 42,
  "userId": "7",
  "topic": "Spring Boot",
  "questionScores": [
    {
      "questionId": "b1f0c1d2-...",
      "questionText": "...",
      "answerText": "...",
      "score": 7.5,
      "maxScore": 10.0,
      "feedback": "...",
      "skillName": "Spring Core"
    }
  ]
}
```
`sessionId` dùng cùng kiểu **Long** ở mọi sự kiện; `userId` là **String** (ADR-010). `skillName` lấy từ topic/tag của câu hỏi để report tính điểm và tiến bộ theo kỹ năng. `maxScore` lấy từ rubric của câu hỏi.

### 5.2.3 Quy ước cho sự kiện mới

- Tên topic `snake_case`, thì quá khứ (`resume_parsed`).
- Payload có `eventId` (UUID), `occurredAt` (ISO-8601 UTC), `version`.
- Consumer idempotent theo khóa nghiệp vụ; lỗi không phục hồi → dead-letter topic `<topic>.DLT`.

## 5.3 Giao thức WebSocket (interview)

| Thuộc tính | Giá trị |
|---|---|
| Endpoint | `/ws/interview` (SockJS), allowed origins giới hạn theo domain frontend |
| Application prefix | `/app` |
| Broker | Simple broker, user destination `/user/queue/interview` |
| Xác thực | JWT trong header `Authorization` của STOMP CONNECT |

| Hướng | Destination | Payload | Kết quả |
|---|---|---|---|
| Client → Server | `/app/interview/{sessionId}/start` | – | Gửi câu thứ nhất; không có câu → `SESSION_END` (total 0) |
| Client → Server | `/app/interview/{sessionId}/answer` | `{questionId, answerText}` | Lưu trả lời, gửi câu kế; hết câu → kết thúc phiên + `SESSION_END` |
| Server → Client | `/user/queue/interview` | `QuestionMessage` (kèm `sessionId`) | Chỉ chủ phiên nhận được |

`QuestionMessage`:
```json
{ "type": "QUESTION", "questionId": "…", "questionText": "…", "questionOrder": 2, "totalQuestions": 5 }
```
`type` ∈ `QUESTION` | `SESSION_END` | `ERROR`.

```mermaid
sequenceDiagram
    participant C as Client (stompjs)
    participant S as InterviewWebSocketController
    participant A as AnswerService
    participant SS as SessionService
    C->>S: CONNECT (Authorization: Bearer JWT)
    C->>S: SUBSCRIBE /user/queue/interview
    C->>S: SEND /app/interview/42/start
    S-->>C: QUESTION order=1
    C->>S: SEND /app/interview/42/answer {questionId, answerText}
    S->>A: submitAnswer()
    alt còn câu
        S-->>C: QUESTION order=2
    else hết câu
        S->>SS: endSession() → COMPLETED + session_completed
        S-->>C: SESSION_END
    end
    Note over S,C: Lỗi (JWT không hợp lệ, không phải chủ phiên, trả lời trùng, phiên không ACTIVE) → ERROR
```

## 5.4 REST liên service

| Caller | Callee | Request | Mục đích | Xử lý lỗi |
|---|---|---|---|---|
| interviews `QuestionServiceClient` | question | `GET /api/v1/questions/random?count=&topic_id=&difficulty_id=` | Lấy câu hỏi cho phiên | 5 s; lỗi → list rỗng → `NoQuestionsAvailableException` |
| grading `InterviewServiceClient` | interviews | `GET /api/v1/interviews/sessions/{id}` + `X-User-Id` | Lấy câu hỏi + câu trả lời | 10 s; lỗi → throw → Kafka retry |
| grading `QuestionServiceClient` | question | `GET /api/v1/questions/{id}` + rubric | Lấy rubric, topic/tag để chấm và gán `skillName` | 10 s; lỗi → throw → Kafka retry |
| question `ResumeAggregateClient` | resume | `GET /api/v1/resumes/{resumeId}/profile` | Dữ liệu CV để AI sinh câu hỏi | – |

Base URL cấu hình: `question-service.base-url`, `interview-service.base-url`, `clients.resume.base-url`.

interviews truyền `topic_id`, `difficulty_id` theo lựa chọn của người dùng khi tạo phiên để câu hỏi đúng topic và độ khó.

## 5.5 Hợp đồng chung cho REST

| Mục | Quy ước |
|---|---|
| Version | Tiền tố `/v1` |
| JSON | `camelCase` cho mọi service |
| Phân trang | `page` (0-based), `size` (giới hạn 100), `sort=field,dir` |
| Lỗi | `{ code, message, timestamp, path, errors? }`, code dạng `<PREFIX>_<số>` (`AUTH_*`, `QUE_*`, `FILE_*`, `JOB_*`, `REVIEW_*`, `GRD_*`, `RPT_*`) |
| Định danh | `Authorization: Bearer <JWT>`; qua gateway thêm `X-User-Id`, `X-User-Role`, `X-User-Name` |
| Tài liệu | springdoc: `/v3/api-docs` hoặc `/api-docs`, `/swagger-ui.html` |

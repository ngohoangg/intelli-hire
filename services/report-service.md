# Report service – Thiết kế chi tiết

Report service lập báo cáo từ kết quả chấm, tính điểm theo kỹ năng, thống kê và theo dõi tiến bộ của người dùng.

| Thuộc tính | Giá trị |
|---|---|
| Repo | `intelli-hire-report` |
| Stack | Spring Boot 3.2.1, Java 17, Web, Data JPA, Cache (Redis), Spring Kafka, Flyway, springdoc |
| Port / context | `${SERVER_PORT:8085}` / `${CONTEXT_PATH:/api}` |
| DB | PostgreSQL, schema mặc định, `V1__init.sql` |
| Package gốc | `org.aibles.intellihirereport` |

## 1. Trách nhiệm

- Consume `grading_completed` → tạo báo cáo, điểm từng câu, điểm kỹ năng.
- Cập nhật tiến bộ người dùng theo kỹ năng.
- API báo cáo, lịch sử, thống kê, tiến bộ; cập nhật tóm tắt/roadmap.

## 2. Mô hình dữ liệu

```mermaid
erDiagram
    interview_reports ||--o{ question_scores : has
    interview_reports ||--o{ skill_scores : has

    interview_reports {
        varchar id PK "UUID"
        varchar session_id UK
        bigint user_id
        varchar topic
        double total_score
        double max_score
        text summary
        jsonb roadmap_json
        int total_questions
        varchar status "PENDING|COMPLETED|FAILED"
    }
    question_scores {
        varchar id PK
        varchar report_id FK "CASCADE"
        varchar question_id
        text question_text
        text answer_text
        double score
        double max_score "10"
        text feedback
        varchar skill_name
    }
    skill_scores {
        varchar id PK
        varchar report_id FK "CASCADE"
        varchar skill_name
        double score
        double max_score
        int question_count
        text feedback
    }
    user_progress {
        varchar id PK
        bigint user_id "UQ(user_id, skill_name)"
        varchar skill_name
        double latest_score
        double average_score
        int total_attempts
        jsonb score_trend "≤ 50 giá trị"
    }
```

`BaseEntity`: `id` UUID, `created_at`, `created_by`, `last_updated_at`, `last_updated_by` (auditor từ `X-User-Name`, mặc định `SYSTEM`).

## 3. API – `/api/v1/reports`

| Method | Path | Mô tả |
|---|---|---|
| POST | `/` | Tạo báo cáo (cùng logic với Kafka) |
| PUT | `/{id}` | Cập nhật `summary`, `roadmapJson` |
| GET | `/{id}` | Theo id |
| GET | `/session/{sessionId}` | Theo phiên |
| DELETE | `/session/{sessionId}` | Xóa theo phiên |
| GET | `/history/{userId}` | Lịch sử phân trang, lọc `status` |
| GET | `/stats/{userId}` | Thống kê |
| GET | `/progress/{userId}` | Tiến bộ mọi kỹ năng |
| GET | `/progress/{userId}/{skillName}` | Tiến bộ một kỹ năng |
| POST | `/{id}/regenerate` | Thay `roadmap_json` |
| GET | `/admin/all` | Toàn bộ; cần `X-User-Role` = `ADMIN`/`ROLE_ADMIN` (không → `RPT_005`) |

Kích thước trang kẹp 1–100.

## 4. Thuật toán

### 4.1 Tạo báo cáo (`ReportServiceImpl.create`)
```mermaid
flowchart TD
    A[GradingCompletedEvent] --> B{Đã có report cho session?}
    B -- có --> X[RPT_002]
    B -- chưa --> C[Lưu report COMPLETED,<br/>total_questions = số câu]
    C --> D[Lưu question_scores]
    D --> E[Nhóm theo skill_name không rỗng]
    E --> F[skill score = Σscore, max = Σmax, count = n]
    F --> G[Report total = Σscore, max = Σmax<br/>null tính là 0]
    G --> H[Mỗi skill: updateProgress userId, skill, score/count]
    H --> I[Evict cache]
```

### 4.2 Cập nhật tiến bộ (`updateProgress`)
Khóa dòng `SELECT … FOR UPDATE` theo (user, skill):

```text
latest   = score
average  = (average × attempts + score) / (attempts + 1)
attempts = attempts + 1
trend    = (trend + [score]) giữ 50 giá trị cuối
```
Chưa có dòng → tạo mới với `attempts = 1`.

### 4.3 Thống kê (`/stats`)
- `totalSessions` = số báo cáo của user.
- Trung bình / cao nhất / thấp nhất của `total_score / max_score × 100` (chỉ khi `max_score > 0`), làm tròn 2 chữ số.
- `topSkills` = 3 kỹ năng `average_score` cao nhất; `weakSkills` = 3 thấp nhất.

## 5. Kafka consumer

| Thuộc tính | Giá trị |
|---|---|
| Topic | `${KAFKA_TOPIC_GRADING_COMPLETED:grading_completed}` |
| Group | `report-service`, `earliest` |
| Deserializer | JSON, không type header, `ignoreUnknown` |
| Lỗi | Bắt và log, **không retry, không DLT** |

## 6. Cache (Redis, GenericJackson2Json)

| Cache | Key | TTL |
|---|---|---|
| `report` | id | 1h |
| `report_session` | sessionId | 1h |
| `user_stats` | userId | 30' |
| `user_progress` | userId | 30' |
| `user_progress_skill` | `userId:skill` | 30' |

Evict khi create, update, regenerate, delete.

## 7. Cấu trúc package

```
intellihirereport/
├── config      Kafka, Redis, JPA auditing, OpenAPI
├── controller  ReportController
├── dto         + dto/kafka (GradingCompletedEvent, QuestionScoreEvent)
├── entity      + enums (ReportStatus)
├── exception   RPT_001..007, PRG_001
├── kafka       GradingCompletedConsumer
├── mapper · repository
├── service     + impl (ReportServiceImpl, UserProgressServiceImpl)
└── utils
```

## 8. Điểm lệch cần xử lý ⚠️

1. **Không có Spring Security**: mọi endpoint mở; `RPT_007` (chỉ xem dữ liệu của mình) khai báo nhưng chưa dùng.
2. `user_id` là BIGINT trong khi các service khác dùng String → cần thống nhất kiểu định danh.
3. Lỗi consumer bị nuốt → mất báo cáo; cần retry + DLT.
4. `skill_name` từ grading luôn null → `skill_scores` và `user_progress` rỗng.
5. `summary` / `roadmap_json` chưa có nguồn sinh tự động (chưa gọi LLM).

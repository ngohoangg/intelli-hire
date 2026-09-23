# IntelliHire Report Service — API Documentation

**Version:** 1.0
**Base URL:** `http://localhost:8085/api`
**Port:** 8085
**Context Path:** `/api`

---

## 1. Overview

Report Service chịu trách nhiệm:
- Nhận kết quả chấm điểm từ Grading Service
- Tổng hợp điểm theo skill, tính tổng điểm
- Sinh báo cáo chi tiết cho mỗi buổi phỏng vấn
- Theo dõi tiến trình kỹ năng của user qua nhiều sessions

---

## 2. Authentication

Tất cả request cần header:
```
X-User-Name: <username>
```
(Được inject bởi API Gateway từ JWT token)

---

## 3. Response Format

### Success Response
```json
{
  "success": true,
  "error_code": null,
  "data": { ... },
  "errors": null,
  "timestamp": "2026-03-16T10:00:00"
}
```

### Error Response
```json
{
  "success": false,
  "error_code": "RPT_001",
  "data": null,
  "errors": null,
  "timestamp": "2026-03-16T10:00:00"
}
```

### Validation Error Response
```json
{
  "success": false,
  "error_code": "COM_001",
  "data": null,
  "errors": [
    {
      "field": "session_id",
      "message": "Session ID is required",
      "rejected_value": ""
    }
  ],
  "timestamp": "2026-03-16T10:00:00"
}
```

---

## 4. Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `RPT_001` | 404 | Report not found |
| `RPT_002` | 409 | Report already exists for session |
| `RPT_003` | 400 | Invalid session id |
| `SKL_001` | 404 | Skill score not found |
| `PRG_001` | 404 | User progress not found |
| `COM_001` | 400 | Validation error |
| `COM_002` | 500 | Internal server error |
| `COM_003` | 400 | Bad request |

---

## 5. API Endpoints

### 5.1 Health Check

**`GET /v1/health`**

```bash
curl http://localhost:8085/api/v1/health
```

**Response:**
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "intelli-hire-report"
  }
}
```

---

### 5.2 Create Report

**`POST /v1/reports`**

Grading Service gọi endpoint này để gửi kết quả chấm điểm. Service sẽ:
1. Tạo report
2. Lưu điểm từng câu hỏi (question_scores)
3. Tự động tính điểm theo skill (skill_scores)
4. Tính tổng điểm (total_score, max_score)
5. Cập nhật user_progress

**Request Body:**
```json
{
  "session_id": "session-001",
  "user_id": 1,
  "topic": "Backend Java",
  "summary": "Ứng viên có kiến thức tốt về Java Core...",
  "roadmap_json": {
    "recommendations": [
      {
        "skill": "Database Optimization",
        "priority": "HIGH",
        "resources": ["SQL Performance Explained"]
      }
    ],
    "next_focus": "Database Optimization",
    "estimated_improvement_weeks": 4
  },
  "question_scores": [
    {
      "question_id": "q-001",
      "question_text": "Explain ArrayList vs LinkedList",
      "answer_text": "ArrayList uses dynamic array...",
      "score": 8.5,
      "max_score": 10,
      "feedback": "Good explanation...",
      "skill_name": "Java Core"
    }
  ]
}
```

**Validation Rules:**
| Field | Required | Rules |
|-------|----------|-------|
| `session_id` | Yes | Not blank |
| `user_id` | Yes | Not null |
| `topic` | No | - |
| `summary` | No | - |
| `roadmap_json` | No | Any JSON object |
| `question_scores` | Yes | Not empty list |
| `question_scores[].question_id` | Yes | Not blank |
| `question_scores[].score` | Yes | 0 - 100 |
| `question_scores[].max_score` | Yes | 1 - 100 |

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": "uuid-report-id",
    "session_id": "session-001",
    "user_id": 1,
    "topic": "Backend Java",
    "total_score": 34.0,
    "max_score": 50.0,
    "summary": "...",
    "roadmap_json": { ... },
    "total_questions": 5,
    "status": "COMPLETED",
    "skill_scores": [
      {
        "id": "uuid",
        "skill_name": "Java Core",
        "score": 15.5,
        "max_score": 20.0,
        "question_count": 2,
        "feedback": null
      }
    ],
    "question_scores": [
      {
        "id": "uuid",
        "question_id": "q-001",
        "question_text": "...",
        "answer_text": "...",
        "score": 8.5,
        "max_score": 10.0,
        "feedback": "...",
        "skill_name": "Java Core",
        "created_at": "2026-03-16T10:00:00"
      }
    ],
    "created_at": "2026-03-16T10:00:00",
    "last_updated_at": "2026-03-16T10:00:00"
  }
}
```

**Error Cases:**
- `409 RPT_002` — session_id đã có report
- `400 COM_001` — validation error

---

### 5.3 Get Report by Session ID

**`GET /v1/reports/session/{sessionId}`**

```bash
curl http://localhost:8085/api/v1/reports/session/session-001
```

**Response:** `200 OK` — Full ReportResponse (same as create)

**Error:** `404 RPT_001` — Report not found

---

### 5.4 Get Report by ID

**`GET /v1/reports/{id}`**

```bash
curl http://localhost:8085/api/v1/reports/uuid-report-id
```

**Response:** `200 OK` — Full ReportResponse

**Error:** `404 RPT_001` — Report not found

---

### 5.5 Get Report History

**`GET /v1/reports/history/{userId}`**

Lấy danh sách report của user, có pagination.

**Query Parameters:**
| Param | Default | Description |
|-------|---------|-------------|
| `page` | 0 | Page number (0-based) |
| `size` | 10 | Page size (1-100) |
| `sort` | `createdAt,desc` | Sort field and direction |

**Sortable Fields:** `createdAt`, `lastUpdatedAt`, `totalScore`

```bash
curl "http://localhost:8085/api/v1/reports/history/1?page=0&size=10&sort=totalScore,desc"
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "session_id": "session-001",
        "topic": "Backend Java",
        "total_score": 34.0,
        "max_score": 50.0,
        "total_questions": 5,
        "status": "COMPLETED",
        "created_at": "2026-03-16T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "total_elements": 1,
    "total_pages": 1,
    "is_first": true,
    "is_last": true
  }
}
```

---

### 5.6 Get User Progress

**`GET /v1/reports/progress/{userId}`**

Lấy tiến trình kỹ năng của user qua nhiều sessions.

```bash
curl http://localhost:8085/api/v1/reports/progress/1
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "user_id": 1,
      "skill_name": "Java Core",
      "latest_score": 8.0,
      "average_score": 7.75,
      "total_attempts": 2,
      "score_trend": [7.75, 8.0],
      "last_updated_at": "2026-03-16T10:00:00"
    },
    {
      "id": "uuid",
      "user_id": 1,
      "skill_name": "Database",
      "latest_score": 7.5,
      "average_score": 6.25,
      "total_attempts": 2,
      "score_trend": [5.0, 7.5],
      "last_updated_at": "2026-03-16T10:00:00"
    }
  ]
}
```

---

### 5.7 Delete Report

**`DELETE /v1/reports/session/{sessionId}`**

Xóa report và tất cả scores liên quan (cascade delete).

```bash
curl -X DELETE http://localhost:8085/api/v1/reports/session/session-001
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": null
}
```

**Error:** `404 RPT_001` — Report not found

---

## 6. Database Schema

### Tables

#### interview_reports
| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR(36) PK | UUID |
| session_id | VARCHAR(36) UNIQUE | Interview session ID |
| user_id | BIGINT | User ID from Auth Service |
| topic | VARCHAR(255) | Interview topic |
| total_score | DOUBLE | Sum of all question scores |
| max_score | DOUBLE | Sum of all max scores |
| summary | TEXT | AI-generated summary |
| roadmap_json | JSONB | Improvement roadmap |
| total_questions | INTEGER | Number of questions |
| status | VARCHAR(20) | PENDING / COMPLETED / FAILED |
| created_at | TIMESTAMP | Auto-generated |
| created_by | VARCHAR(255) | From X-User-Name header |

#### skill_scores
| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR(36) PK | UUID |
| report_id | VARCHAR(36) FK | → interview_reports(id) CASCADE |
| skill_name | VARCHAR(255) | e.g. "Java Core", "Database" |
| score | DOUBLE | Total score for this skill |
| max_score | DOUBLE | Total max score for this skill |
| question_count | INTEGER | Questions in this skill |
| feedback | TEXT | Skill-level feedback |

#### question_scores
| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR(36) PK | UUID |
| report_id | VARCHAR(36) FK | → interview_reports(id) CASCADE |
| question_id | VARCHAR(36) | From Question Service |
| question_text | TEXT | Question content |
| answer_text | TEXT | User's answer |
| score | DOUBLE | Score for this answer |
| max_score | DOUBLE | Maximum possible score |
| feedback | TEXT | AI feedback |
| skill_name | VARCHAR(255) | Associated skill |

#### user_progress
| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR(36) PK | UUID |
| user_id | BIGINT | User ID |
| skill_name | VARCHAR(255) | Skill name |
| latest_score | DOUBLE | Most recent score |
| average_score | DOUBLE | Running average |
| total_attempts | INTEGER | Number of attempts |
| score_trend | JSONB | Array of historical scores |

**Unique Constraint:** `(user_id, skill_name)` on user_progress

---

## 7. Business Logic

### Score Calculation Flow
```
1. Receive question scores from Grading Service
2. Group questions by skill_name
3. For each skill: sum(score), sum(max_score), count(questions)
4. Total score = sum of all question scores
5. Max score = sum of all question max_scores
6. Save skill_scores and update user_progress
```

### User Progress Update
```
For each skill in the report:
  avg_skill_score = skill_total_score / question_count

  If user has existing progress for this skill:
    new_attempts = old_attempts + 1
    new_average = (old_average * old_attempts + avg_skill_score) / new_attempts
    append avg_skill_score to score_trend
  Else:
    Create new progress with attempts=1, average=avg_skill_score
```

### Concurrency
- `user_progress` updates use **pessimistic locking** (`SELECT ... FOR UPDATE`) to prevent race conditions when multiple reports are created concurrently for the same user.

---

## 8. Missing APIs (Planned for future)

Xem mục dưới.

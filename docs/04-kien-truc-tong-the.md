# 04 – Kiến trúc tổng thể (HLD)

IntelliHire được thiết kế theo kiến trúc microservice gồm 6 service Spring Boot, mỗi service sở hữu một schema PostgreSQL; giao tiếp đồng bộ qua REST và bất đồng bộ qua Kafka cho chuỗi phỏng vấn → chấm → báo cáo.

## 4.1 Sơ đồ thành phần

### Sơ đồ thành phần logic

Ở môi trường phát triển, frontend gọi thẳng từng service qua dev proxy của Angular.

```mermaid
flowchart TB
    FE["Angular SPA<br/>:4200 (ng serve + proxy.conf.json)"]

    subgraph Services
        AUTH["auth-service<br/>:8090"]
        QUE["question-service<br/>:8081 · /api"]
        RES["resume-service<br/>:8082"]
        INT["interview-service<br/>:8083 · REST + WS"]
        GRA["grading-service<br/>:8084 · /api"]
        REP["report-service<br/>:8085 · /api"]
    end

    subgraph Infra
        PG[(PostgreSQL<br/>schema/service)]
        RD[(Redis)]
        KF{{Kafka}}
        MN[(MinIO)]
    end

    subgraph External
        LLM[[LLM: OpenAI / Claude /<br/>Gemini / Grok / OpenRouter]]
        SMTP[[SMTP]]
        OA[[Google / GitHub OAuth]]
    end

    FE -->|/api/v1/auth, oauth, profiles, accounts, admin| AUTH
    FE -->|/api/v1/questions, topics, tags...| QUE
    FE -->|/api/v1/resumes, skills, parse-jobs| RES
    FE -->|/api/v1/interviews + /ws/interview| INT
    FE -->|/api/v1/gradings| GRA
    FE -->|/api/v1/reports| REP

    INT -->|GET /questions/random| QUE
    QUE -->|GET /resumes/:id/profile| RES
    GRA -->|GET /interviews/sessions/:id| INT
    GRA -->|GET /questions/:id + rubric| QUE
    INT -- session_completed --> KF
    KF -- session_completed --> GRA
    GRA -- grading_completed --> KF
    KF -- grading_completed --> REP

    AUTH --- PG & RD
    QUE --- PG & RD
    RES --- PG & RD & MN
    INT --- PG
    GRA --- PG & RD
    REP --- PG & RD

    AUTH -.-> SMTP & OA
    RES -.-> LLM
    QUE -.-> LLM
    GRA -.-> LLM
```

### Định tuyến qua API Gateway

Ở môi trường staging/production, mọi request đi qua API Gateway.

```mermaid
flowchart LR
    FE[Angular SPA] --> GW[API Gateway :8080<br/>verify JWT · route · CORS · rate limit]
    GW --> EU[(Eureka :8761)]
    GW -->|lb://auth-service| AUTH[auth]
    GW -->|lb://question-service| QUE[question]
    GW -->|lb://resume-service| RES[resume]
    GW -->|lb://interview-service| INT[interviews]
    GW -->|lb://grading-service| GRA[grading]
    GW -->|lb://report-service| REP[report]
    AUTH & QUE & RES & INT & GRA & REP -. register .-> EU
```

Gateway xác thực JWT một lần và chuyển tiếp `X-User-Id`, `X-User-Role`, `X-User-Name` cho các service phía sau. Xem [services/gateway-eureka.md](services/gateway-eureka.md).

## 4.2 Danh mục service

| Service | Trách nhiệm | Port | Context path | Schema DB | Redis | Kafka | Ngoài |
|---|---|---|---|---|---|---|---|
| auth | Danh tính, JWT, OAuth, RBAC, phiên | 8090 (`SERVER_PORT`) | `/` | `${DB_SCHEMA}` | Hoạt động phiên | – | SMTP, Google, GitHub |
| question | Ngân hàng câu hỏi, rubric, kiểm duyệt, topic/tag | 8081 | `/api` | `question` | Cache + random pool | – | OpenRouter |
| resume | CV, file, parse AI, review | 8082 | – (path có `/api`) | mặc định | Cache + hàng đợi parse | – | MinIO, LLM |
| interviews | Phiên phỏng vấn, WebSocket | 8083 | – | `interviews` | – | Produce | – |
| grading | Chấm bằng LLM | 8084 | `/api` | mặc định | Cache | Consume + produce | OpenRouter |
| report | Báo cáo, thống kê, tiến bộ | 8085 | `/api` | mặc định | Cache | Consume | – |

## 4.3 Công nghệ

| Lớp | Công nghệ |
|---|---|
| Backend | Java 17/21, Spring Boot 3.2–3.4, Spring Web, Data JPA, Validation, Cache, WebSocket (STOMP), Spring Kafka, Spring Security (auth) |
| Mapping / tiện ích | MapStruct, Lombok, springdoc-openapi, jjwt |
| Dữ liệu | PostgreSQL 14+ (Flyway, JSONB), Redis 7 |
| Messaging | Apache Kafka (JSON serializer, không type header) |
| Lưu file | MinIO (S3-compatible) |
| Xử lý tài liệu | Apache PDFBox 3, Apache POI |
| AI | REST tới OpenAI / Anthropic / Gemini / xAI / OpenRouter |
| Frontend | Angular 19 standalone, Angular Material, RxJS, @stomp/stompjs + SockJS |
| Test | JUnit 5, Mockito, Testcontainers (resume), Karma/Jasmine (FE) |
| Đóng gói | Docker, docker-compose từng service |

## 4.4 Kiểu giao tiếp

| Từ → Đến | Kiểu | Mục đích | Timeout / retry |
|---|---|---|---|
| FE → service | REST/JSON | Mọi thao tác người dùng | FE retry 1 lần sau refresh token |
| FE ↔ interviews | STOMP over SockJS | Hỏi–đáp thời gian thực | reconnect do client |
| interviews → question | REST | Lấy câu hỏi ngẫu nhiên | 5 s, lỗi → danh sách rỗng |
| question → resume | REST | Hồ sơ tổng hợp để sinh câu hỏi | – |
| grading → interviews | REST | Lấy câu hỏi + câu trả lời của phiên | 10 s, lỗi → Kafka retry |
| grading → question | REST | Lấy rubric, topic/tag của câu hỏi | 10 s, lỗi → Kafka retry |
| interviews → grading | Kafka `session_completed` | Kích hoạt chấm | 3 retry × 5 s, lỗi → DLT |
| grading → report | Kafka `grading_completed` | Kích hoạt lập báo cáo | Retry có giới hạn, lỗi → DLT |
| resume (nội bộ) | Redis list `resume:parse:queue` | Hàng đợi parse CV | retry 2/4/8 s cho LLM |

## 4.5 Mẫu kiến trúc nội bộ

| Service | Mẫu | Ghi chú |
|---|---|---|
| auth | Hexagonal theo feature: `adapter/web`, `adapter/repository`, `usecase`, `domain` | Use case = interface + Impl, command record |
| còn lại | Layered: `controller` → `service`/`impl` → `repository`; `dto`, `mapper`, `entity`, `exception`, `config` | Không dùng quan hệ JPA; FK là cột String/Long |

Nguyên tắc chung: database-per-service, không truy cập bảng của service khác; ID dạng UUID String (question, resume, grading, report) hoặc BIGINT identity (auth, interviews).

## 4.6 Sơ đồ triển khai

```mermaid
flowchart TB
    U[Trình duyệt] -->|HTTPS| NG[Nginx: static SPA + TLS]
    NG -->|/api, /ws| GW[Gateway]
    subgraph Docker network
        GW --> S1[auth] & S2[question] & S3[resume] & S4[interviews] & S5[grading] & S6[report]
        S1 & S2 & S3 & S4 & S5 & S6 --> PG[(postgres:16)]
        S1 & S2 & S3 & S5 & S6 --> RD[(redis:7)]
        S4 & S5 & S6 --> KF[(kafka)]
        S3 --> MN[(minio)]
    end
```

Chi tiết môi trường và biến cấu hình: [10-trien-khai-van-hanh.md](10-trien-khai-van-hanh.md).

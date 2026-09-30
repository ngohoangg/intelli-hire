# IntelliHire

Nền tảng luyện phỏng vấn kỹ thuật có AI. Tài liệu phân tích & thiết kế: [docs/](docs/README.md).

Repo này là **base code**: đã có cấu trúc thư mục, cấu hình (pom, application.yml, Dockerfile) và class `main` của từng service. Mỗi thành viên code tiếp trong service của mình theo đúng cấu trúc và quy ước dưới đây.

## Cấu trúc

| Thư mục | Service | Port | Context path | Kiến trúc |
|---|---|---|---|---|
| [intelli-hire-eureka-service](intelli-hire-eureka-service/) | Service discovery | 8761 | – | – |
| [intelli-hire-gateway](intelli-hire-gateway/) | API Gateway | 8080 | – | WebFlux |
| [intelli-hire-auth](intelli-hire-auth/) | auth-service | 8090 | `/` | Hexagonal theo feature |
| [intelli-hire-question](intelli-hire-question/) | question-service | 8081 | `/api` | Layered |
| [intelli-hire-resume](intelli-hire-resume/) | resume-service | 8082 | – | Layered |
| [intelli-hire-interviews](intelli-hire-interviews/) | interview-service | 8083 | – | Layered |
| [intelli-hire-grading](intelli-hire-grading/) | grading-service | 8084 | `/api` | Layered |
| [intelli-hire-report](intelli-hire-report/) | report-service | 8085 | `/api` | Layered |

Mỗi service có `README.md` mô tả từng package chứa gì và ví dụ tên class.

## Công nghệ

Java 21 · Spring Boot 3.4.2 · Spring Cloud 2024.0.0 · PostgreSQL 16 + Flyway · Redis 7 · Kafka · MinIO · Lombok · MapStruct · springdoc-openapi · JUnit 5 + Testcontainers · JaCoCo.

## Chạy local

Yêu cầu: JDK 21, Maven 3.9+, Docker.

```bash
# 1. Tạo file .env ở thư mục gốc (xem mục "Biến môi trường")
# 2. Chạy hạ tầng + Eureka
docker compose up -d postgres redis kafka minio eureka
# 3. Chạy service đang phát triển (copy .env vào thư mục service: spring-dotenv đọc .env ở thư mục đang chạy)
cd intelli-hire-question
mvn spring-boot:run
```

Chạy toàn bộ bằng Docker: `docker compose up -d --build`.

## Quy ước chung

### Code

| Mục | Quy ước |
|---|---|
| Đặt tên | `XxxController`, `XxxService` + `XxxServiceImpl`, `XxxRepository`, `XxxMapper`; DTO: `CreateXxxRequest`, `UpdateXxxRequest`, `XxxResponse`; sự kiện Kafka: `XxxEvent` |
| Controller | Chỉ nhận request, `@Valid`, gọi service, trả `BaseResponse`; không chứa logic nghiệp vụ, không gọi repository |
| Service | Interface + `impl`; `@Transactional` ở impl, `readOnly = true` cho hàm đọc |
| Mapper | MapStruct `@Mapper(componentModel = "spring")`, không map tay trong service |
| Entity | Kế thừa `BaseEntity` (`id` UUID String, `createdAt`, `createdBy`, `updatedAt`, `updatedBy`); không dùng quan hệ JPA (`@OneToMany`...), khóa ngoại là cột thuần (ADR-008) |
| Lombok | Entity: `@Getter @Setter @SuperBuilder @NoArgsConstructor`; DTO: `@Data @Builder` hoặc `record`; bean: `@RequiredArgsConstructor` (không `@Autowired` field) |
| Cấu hình | Dùng `@ConfigurationProperties` + `@Validated` dưới prefix `application.*`, không rải `@Value` |
| Log | `@Slf4j`, placeholder `{}`; không log token, mật khẩu, nội dung CV |

### API

| Mục | Quy ước |
|---|---|
| URL | `/v1/<danh-từ-số-nhiều>` (vd. `/v1/questions/{id}`); admin: `/v1/admin/...` |
| JSON | `camelCase` |
| Danh tính | Đọc `X-User-Id`, `X-User-Role`, `X-User-Name` do gateway gắn qua `HeaderAuthFilter`; không nhận `userId` từ body |
| Phân trang | `page` (từ 0), `size` (tối đa 100), `sort=field,asc\|desc`; trả `PageResponse<T>` |
| Tài liệu | Mỗi controller có `@Tag`, mỗi API có `@Operation`; Swagger tại `/swagger-ui.html` |

Response thành công:

```json
{ "success": true, "data": { }, "timestamp": "2026-09-30T10:00:00Z" }
```

Response lỗi:

```json
{
  "success": false,
  "code": "QUE_001",
  "message": "Question not found",
  "path": "/api/v1/questions/abc",
  "errors": [ { "field": "title", "message": "must not be blank" } ],
  "timestamp": "2026-09-30T10:00:00Z"
}
```

Mã lỗi `<PREFIX>_<số>`, khai báo trong enum `ErrorCode` kèm `HttpStatus`:

| Service | Prefix |
|---|---|
| auth | `AUTH_` |
| question | `QUE_` |
| resume | `RES_`, `FILE_`, `JOB_`, `REVIEW_` |
| interviews | `INT_` |
| grading | `GRD_` |
| report | `RPT_` |
| Lỗi chung (validation, 500) | `COM_` |

### Database

- Mỗi service một schema (`auth`, `question`, `resume`, `interviews`, `grading`, `report`), không truy cập schema của service khác (ADR-001).
- Migration Flyway trong `src/main/resources/db/migration`: `V1__create_questions.sql`, `V2__add_index_questions_status.sql`… Không sửa file đã merge, thêm file mới.
- `ddl-auto: validate`: schema chỉ thay đổi qua Flyway.
- `user_id` luôn là `VARCHAR(64)` (ADR-010).

### Kafka

- Topic `snake_case` thì quá khứ: `session_completed`, `grading_completed`.
- Payload là DTO trong `dto/event`, đúng hợp đồng ở [docs/05](docs/05-thiet-ke-tich-hop.md); không gửi type header.
- Consumer idempotent theo khóa nghiệp vụ, lỗi → retry có giới hạn → `<topic>.DLT`.

### Test

- Unit test service (Mockito), `@WebMvcTest` cho controller, `@DataJpaTest` + Testcontainers cho repository; không dùng H2.
- Tên hàm: `method_should_expected_when_condition`.
- Coverage service layer ≥ 70% (`mvn verify` → `target/site/jacoco`).
- Chỉ dùng dữ liệu giả; LLM được mock.

### Git

- Nhánh: `main` (ổn định), `feature/<service>-<mô-tả>`, `fix/<service>-<mô-tả>`.
- Commit: `feat(question): add random question API`, `fix(auth): ...`, `test(...)`, `docs(...)`.
- Mỗi PR cần 1 người review; build + test xanh trước khi merge.

## Biến môi trường

Tạo file `.env` ở thư mục gốc (không commit) với nội dung sau, sửa giá trị cho phù hợp:

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=intellihire
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis / Kafka / Eureka
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_SERVERS=localhost:9092
EUREKA_URL=http://localhost:8761/eureka

# JWT (auth + gateway dùng chung)
JWT_SECRET=<chuỗi ngẫu nhiên ≥ 32 byte>
JWT_ISSUER=intellihire-auth
JWT_ACCESS_AUDIENCE=intellihire-api
JWT_REFRESH_AUDIENCE=intellihire-refresh
JWT_ACCESS_EXPIRATION_SECONDS=900
JWT_REFRESH_EXPIRATION_SECONDS=604800

# Mật khẩu
PASSWORD_MAX_FAILED_ATTEMPTS=5
PASSWORD_LOCK_DURATION_MINUTES=15

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_FROM=noreply@intellihire.com
APP_BASE_URL=http://localhost:4200

# OAuth
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_REDIRECT_URI=http://localhost:4200/auth/callback/google
GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=
GITHUB_REDIRECT_URI=http://localhost:4200/auth/callback/github

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=resumes

# AI
AI_PROVIDER=openrouter
OPENROUTER_API_KEY=
OPENROUTER_MODEL=

# URL service
QUESTION_SERVICE_URL=http://localhost:8081
RESUME_SERVICE_URL=http://localhost:8082
INTERVIEW_SERVICE_URL=http://localhost:8083

# Kafka topic
KAFKA_TOPIC_SESSION_COMPLETED=session_completed
KAFKA_TOPIC_GRADING_COMPLETED=grading_completed

# CORS / WebSocket
CORS_ALLOWED_ORIGINS=http://localhost:4200
WS_ALLOWED_ORIGINS=http://localhost:4200
```

`DB_SCHEMA` không đặt trong `.env`: mỗi service tự có giá trị mặc định trong `application.yml`.

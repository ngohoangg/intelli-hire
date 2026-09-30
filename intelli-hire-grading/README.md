# intelli-hire-grading

IntelliHire - Chấm điểm bằng LLM. Thiết kế chi tiết: [docs/services/grading-service.md](../docs/services/grading-service.md).

| Thuộc tính | Giá trị |
|---|---|
| Tên service (Eureka) | `grading-service` |
| Port | `8084` |
| Context path | /api |
| Schema DB | `grading` |
| Package gốc | `org.aibles.intellihiregrading` |
| Class main | `IntelliHireGradingApplication` |

## Chạy local

```bash
# Tạo file .env ở thư mục gốc theo mục "Biến môi trường" trong README gốc
docker compose -f ../docker-compose.yml up -d postgres redis kafka minio eureka
mvn spring-boot:run
```

Swagger: `http://localhost:8084/api/swagger-ui.html`

## Cấu trúc package

Kiến trúc **layered**: `controller` → `service` → `repository`.

Gốc: `src/main/java/org/aibles/intellihiregrading/`

| Package | Chứa gì | Ví dụ tên class |
|---|---|---|
| `client/` | REST client gọi service khác (WebClient) | `InterviewServiceClient`, `QuestionServiceClient` |
| `client/dto/` | DTO của service được gọi | `InterviewSessionDto`, `QuestionRubricDto` |
| `config/` | Cấu hình Spring (`@Configuration`, `@ConfigurationProperties`) | `RedisConfig`, `KafkaConsumerConfig`, `AsyncConfig`, `OpenApiConfig` |
| `controller/` | REST controller, chỉ nhận request, validate, gọi service, trả `BaseResponse` | `GradingSessionController` |
| `dto/request/` | Body/query đầu vào, có annotation validation | `CreateGradingSessionRequest`, `UpdateGradingSessionRequest` |
| `dto/response/` | Dữ liệu trả ra client; `BaseResponse<T>`, `PageResponse<T>` dùng chung | `GradingSessionResponse` |
| `dto/event/` | Payload sự kiện Kafka (hợp đồng với service khác, xem docs/05) | `SessionCompletedEvent`, `GradingCompletedEvent` |
| `dto/ai/` | DTO gọi / nhận từ LLM | `GradingPrompt`, `GradingAiResult` |
| `entity/` | JPA entity, kế thừa `BaseEntity` (id, createdAt, createdBy, updatedAt, updatedBy) | `GradingSession` |
| `entity/enums/` | Enum dùng trong entity | `GradingSessionStatus` |
| `exception/` | `BaseException`, `ErrorCode` (mã `<PREFIX>_<số>`), `GlobalExceptionHandler` | `NotFoundException`, `ErrorCode.GRD_001` |
| `filter/` | `HeaderAuthFilter` (đọc `X-User-Id`, `X-User-Role` từ gateway, thiếu → 401), `MdcLoggingFilter` (requestId, userId) | `HeaderAuthFilter` |
| `kafka/` | Producer / consumer Kafka | `SessionCompletedConsumer`, `GradingCompletedProducer` |
| `mapper/` | MapStruct `@Mapper(componentModel = "spring")` entity ↔ DTO | `GradingSessionMapper` |
| `repository/` | Spring Data JPA repository | `GradingSessionRepository` |
| `service/` | Interface nghiệp vụ | `GradingSessionService` |
| `service/impl/` | Hiện thực service, `@Transactional` | `GradingSessionServiceImpl` |
| `service/ai/` | Interface gọi LLM (`AiProvider`) | `AiProvider` |
| `utils/` | Hàm tiện ích tĩnh | `PaginationUtils` |

Resources:

| Đường dẫn | Chứa gì |
|---|---|
| `application.yml` | Cấu hình chung, đọc biến môi trường (xem README gốc) |
| `application-dev.yml`, `application-prod.yml` | Ghi đè theo profile |
| `db/migration/` | Flyway: `V1__create_<bảng>.sql`, `V2__...`; không sửa file đã merge |

## Cấu trúc test

Gốc: `src/test/java/org/aibles/intellihiregrading/`

| Package | Loại test |
|---|---|
| `controller/` | `@WebMvcTest` + MockMvc — `GradingSessionControllerTest` |
| `service/impl/` | Unit test Mockito — `GradingSessionServiceImplTest` |
| `repository/` | `@DataJpaTest` + Testcontainers PostgreSQL — `GradingSessionRepositoryTest` |
| `kafka/` | Test producer/consumer (Testcontainers Kafka) — `<TênClass>Test` |
| `integration/` | `@SpringBootTest` + Testcontainers, luồng đầy đủ — `GradingSessionFlowIntegrationTest` |

Tên phương thức test: `method_should_expected_when_condition`.

Xem quy ước chung ở [README gốc](../README.md).

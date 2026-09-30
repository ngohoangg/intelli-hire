# intelli-hire-interviews

IntelliHire - Phiên phỏng vấn, WebSocket. Thiết kế chi tiết: [docs/services/interview-service.md](../docs/services/interview-service.md).

| Thuộc tính | Giá trị |
|---|---|
| Tên service (Eureka) | `interview-service` |
| Port | `8083` |
| Context path | (không có, mapping tự chứa `/api`) |
| Schema DB | `interviews` |
| Package gốc | `org.aibles.intellihireinterview` |
| Class main | `IntellihireinterviewApplication` |

## Chạy local

```bash
# Tạo file .env ở thư mục gốc theo mục "Biến môi trường" trong README gốc
docker compose -f ../docker-compose.yml up -d postgres redis kafka minio eureka
mvn spring-boot:run
```

Swagger: `http://localhost:8083/swagger-ui.html`

## Cấu trúc package

Kiến trúc **layered**: `controller` → `service` → `repository`.

Gốc: `src/main/java/org/aibles/intellihireinterview/`

| Package | Chứa gì | Ví dụ tên class |
|---|---|---|
| `client/` | REST client gọi service khác (WebClient) | `QuestionServiceClient` |
| `client/dto/` | DTO của service được gọi | `RandomQuestionDto` |
| `config/` | Cấu hình Spring (`@Configuration`, `@ConfigurationProperties`) | `WebSocketConfig`, `KafkaProducerConfig`, `OpenApiConfig` |
| `controller/` | REST controller, chỉ nhận request, validate, gọi service, trả `BaseResponse` | `InterviewSessionController` |
| `websocket/` | Controller/handler STOMP | `InterviewWebSocketController` |
| `dto/request/` | Body/query đầu vào, có annotation validation | `CreateInterviewSessionRequest`, `UpdateInterviewSessionRequest` |
| `dto/response/` | Dữ liệu trả ra client; `BaseResponse<T>`, `PageResponse<T>` dùng chung | `InterviewSessionResponse` |
| `dto/event/` | Payload sự kiện Kafka (hợp đồng với service khác, xem docs/05) | `SessionCompletedEvent` |
| `entity/` | JPA entity, kế thừa `BaseEntity` (id, createdAt, createdBy, updatedAt, updatedBy) | `InterviewSession` |
| `entity/enums/` | Enum dùng trong entity | `InterviewSessionStatus` |
| `exception/` | `BaseException`, `ErrorCode` (mã `<PREFIX>_<số>`), `GlobalExceptionHandler` | `NotFoundException`, `ErrorCode.INT_001` |
| `filter/` | `HeaderAuthFilter` (đọc `X-User-Id`, `X-User-Role` từ gateway, thiếu → 401), `MdcLoggingFilter` (requestId, userId) | `HeaderAuthFilter` |
| `kafka/` | Producer / consumer Kafka | `SessionEventProducer` |
| `mapper/` | MapStruct `@Mapper(componentModel = "spring")` entity ↔ DTO | `InterviewSessionMapper` |
| `repository/` | Spring Data JPA repository | `InterviewSessionRepository` |
| `scheduler/` | Job định kỳ `@Scheduled` | `SessionExpiryScheduler` |
| `service/` | Interface nghiệp vụ | `InterviewSessionService` |
| `service/impl/` | Hiện thực service, `@Transactional` | `InterviewSessionServiceImpl` |
| `utils/` | Hàm tiện ích tĩnh | `PaginationUtils` |

Resources:

| Đường dẫn | Chứa gì |
|---|---|
| `application.yml` | Cấu hình chung, đọc biến môi trường (xem README gốc) |
| `application-dev.yml`, `application-prod.yml` | Ghi đè theo profile |
| `db/migration/` | Flyway: `V1__create_<bảng>.sql`, `V2__...`; không sửa file đã merge |

## Cấu trúc test

Gốc: `src/test/java/org/aibles/intellihireinterview/`

| Package | Loại test |
|---|---|
| `controller/` | `@WebMvcTest` + MockMvc — `InterviewSessionControllerTest` |
| `service/impl/` | Unit test Mockito — `InterviewSessionServiceImplTest` |
| `repository/` | `@DataJpaTest` + Testcontainers PostgreSQL — `InterviewSessionRepositoryTest` |
| `kafka/` | Test producer/consumer (Testcontainers Kafka) — `<TênClass>Test` |
| `integration/` | `@SpringBootTest` + Testcontainers, luồng đầy đủ — `InterviewSessionFlowIntegrationTest` |

Tên phương thức test: `method_should_expected_when_condition`.

Xem quy ước chung ở [README gốc](../README.md).

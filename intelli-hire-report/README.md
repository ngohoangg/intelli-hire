# intelli-hire-report

IntelliHire - Báo cáo, thống kê, tiến bộ. Thiết kế chi tiết: [docs/services/report-service.md](../docs/services/report-service.md).

| Thuộc tính | Giá trị |
|---|---|
| Tên service (Eureka) | `report-service` |
| Port | `8085` |
| Context path | /api |
| Schema DB | `report` |
| Package gốc | `org.aibles.intellihirereport` |
| Class main | `IntelliHireReportApplication` |

## Chạy local

```bash
# Tạo file .env ở thư mục gốc theo mục "Biến môi trường" trong README gốc
docker compose -f ../docker-compose.yml up -d postgres redis kafka minio eureka
mvn spring-boot:run
```

Swagger: `http://localhost:8085/api/swagger-ui.html`

## Cấu trúc package

Kiến trúc **layered**: `controller` → `service` → `repository`.

Gốc: `src/main/java/org/aibles/intellihirereport/`

| Package | Chứa gì | Ví dụ tên class |
|---|---|---|
| `config/` | Cấu hình Spring (`@Configuration`, `@ConfigurationProperties`) | `RedisConfig`, `KafkaConsumerConfig`, `OpenApiConfig` |
| `controller/` | REST controller, chỉ nhận request, validate, gọi service, trả `BaseResponse` | `InterviewReportController` |
| `dto/request/` | Body/query đầu vào, có annotation validation | `CreateInterviewReportRequest`, `UpdateInterviewReportRequest` |
| `dto/response/` | Dữ liệu trả ra client; `BaseResponse<T>`, `PageResponse<T>` dùng chung | `InterviewReportResponse` |
| `dto/event/` | Payload sự kiện Kafka (hợp đồng với service khác, xem docs/05) | `GradingCompletedEvent` |
| `entity/` | JPA entity, kế thừa `BaseEntity` (id, createdAt, createdBy, updatedAt, updatedBy) | `InterviewReport` |
| `entity/enums/` | Enum dùng trong entity | `InterviewReportStatus` |
| `exception/` | `BaseException`, `ErrorCode` (mã `<PREFIX>_<số>`), `GlobalExceptionHandler` | `NotFoundException`, `ErrorCode.RPT_001` |
| `filter/` | `HeaderAuthFilter` (đọc `X-User-Id`, `X-User-Role` từ gateway, thiếu → 401), `MdcLoggingFilter` (requestId, userId) | `HeaderAuthFilter` |
| `kafka/` | Producer / consumer Kafka | `GradingCompletedConsumer` |
| `mapper/` | MapStruct `@Mapper(componentModel = "spring")` entity ↔ DTO | `InterviewReportMapper` |
| `repository/` | Spring Data JPA repository | `InterviewReportRepository` |
| `service/` | Interface nghiệp vụ | `InterviewReportService` |
| `service/impl/` | Hiện thực service, `@Transactional` | `InterviewReportServiceImpl` |
| `utils/` | Hàm tiện ích tĩnh | `PaginationUtils` |

Resources:

| Đường dẫn | Chứa gì |
|---|---|
| `application.yml` | Cấu hình chung, đọc biến môi trường (xem README gốc) |
| `application-dev.yml`, `application-prod.yml` | Ghi đè theo profile |
| `db/migration/` | Flyway: `V1__create_<bảng>.sql`, `V2__...`; không sửa file đã merge |

## Cấu trúc test

Gốc: `src/test/java/org/aibles/intellihirereport/`

| Package | Loại test |
|---|---|
| `controller/` | `@WebMvcTest` + MockMvc — `InterviewReportControllerTest` |
| `service/impl/` | Unit test Mockito — `InterviewReportServiceImplTest` |
| `repository/` | `@DataJpaTest` + Testcontainers PostgreSQL — `InterviewReportRepositoryTest` |
| `kafka/` | Test producer/consumer (Testcontainers Kafka) — `<TênClass>Test` |
| `integration/` | `@SpringBootTest` + Testcontainers, luồng đầy đủ — `InterviewReportFlowIntegrationTest` |

Tên phương thức test: `method_should_expected_when_condition`.

Xem quy ước chung ở [README gốc](../README.md).

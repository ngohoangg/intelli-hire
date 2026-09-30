# intelli-hire-resume

IntelliHire - CV, file, parse AI, review. Thiết kế chi tiết: [docs/services/resume-service.md](../docs/services/resume-service.md).

| Thuộc tính | Giá trị |
|---|---|
| Tên service (Eureka) | `resume-service` |
| Port | `8082` |
| Context path | (không có, mapping tự chứa `/api`) |
| Schema DB | `resume` |
| Package gốc | `org.aibles.intellihireresume` |
| Class main | `IntellihireresumeApplication` |

## Chạy local

```bash
# Tạo file .env ở thư mục gốc theo mục "Biến môi trường" trong README gốc
docker compose -f ../docker-compose.yml up -d postgres redis kafka minio eureka
mvn spring-boot:run
```

Swagger: `http://localhost:8082/swagger-ui.html`

## Cấu trúc package

Kiến trúc **layered**: `controller` → `service` → `repository`.

Gốc: `src/main/java/org/aibles/intellihireresume/`

| Package | Chứa gì | Ví dụ tên class |
|---|---|---|
| `config/` | Cấu hình Spring (`@Configuration`, `@ConfigurationProperties`) | `RedisConfig`, `MinioConfig`, `OpenApiConfig`, `JpaAuditingConfig` |
| `controller/` | REST controller, chỉ nhận request, validate, gọi service, trả `BaseResponse` | `ResumeController` |
| `dto/request/` | Body/query đầu vào, có annotation validation | `CreateResumeRequest`, `UpdateResumeRequest` |
| `dto/response/` | Dữ liệu trả ra client; `BaseResponse<T>`, `PageResponse<T>` dùng chung | `ResumeResponse` |
| `dto/ai/` | DTO gọi / nhận từ LLM | `ParsedResumeResult`, `ResumeReviewResult` |
| `entity/` | JPA entity, kế thừa `BaseEntity` (id, createdAt, createdBy, updatedAt, updatedBy) | `Resume` |
| `entity/enums/` | Enum dùng trong entity | `ResumeStatus` |
| `exception/` | `BaseException`, `ErrorCode` (mã `<PREFIX>_<số>`), `GlobalExceptionHandler` | `NotFoundException`, `ErrorCode.RES_001` |
| `filter/` | `HeaderAuthFilter` (đọc `X-User-Id`, `X-User-Role` từ gateway, thiếu → 401), `MdcLoggingFilter` (requestId, userId) | `HeaderAuthFilter` |
| `mapper/` | MapStruct `@Mapper(componentModel = "spring")` entity ↔ DTO | `ResumeMapper` |
| `repository/` | Spring Data JPA repository | `ResumeRepository` |
| `scheduler/` | Job định kỳ `@Scheduled` | `ParseJobScheduler` |
| `service/` | Interface nghiệp vụ | `ResumeService` |
| `service/impl/` | Hiện thực service, `@Transactional` | `ResumeServiceImpl` |
| `service/ai/` | Interface gọi LLM (`AiProvider`) | `AiProvider` |
| `service/ai/impl/` | Hiện thực theo nhà cung cấp | `OpenRouterAiProvider`, `OpenAiProvider` |
| `utils/` | Hàm tiện ích tĩnh | `PaginationUtils` |

Resources:

| Đường dẫn | Chứa gì |
|---|---|
| `application.yml` | Cấu hình chung, đọc biến môi trường (xem README gốc) |
| `application-dev.yml`, `application-prod.yml` | Ghi đè theo profile |
| `db/migration/` | Flyway: `V1__create_<bảng>.sql`, `V2__...`; không sửa file đã merge |

## Cấu trúc test

Gốc: `src/test/java/org/aibles/intellihireresume/`

| Package | Loại test |
|---|---|
| `controller/` | `@WebMvcTest` + MockMvc — `ResumeControllerTest` |
| `service/impl/` | Unit test Mockito — `ResumeServiceImplTest` |
| `repository/` | `@DataJpaTest` + Testcontainers PostgreSQL — `ResumeRepositoryTest` |
| `integration/` | `@SpringBootTest` + Testcontainers, luồng đầy đủ — `ResumeFlowIntegrationTest` |

Tên phương thức test: `method_should_expected_when_condition`.

Xem quy ước chung ở [README gốc](../README.md).

# intelli-hire-question

IntelliHire - Ngân hàng câu hỏi, rubric, kiểm duyệt. Thiết kế chi tiết: [docs/services/question-service.md](../docs/services/question-service.md).

| Thuộc tính | Giá trị |
|---|---|
| Tên service (Eureka) | `question-service` |
| Port | `8081` |
| Context path | /api |
| Schema DB | `question` |
| Package gốc | `org.aibles.intellihirequestion` |
| Class main | `IntelliHireQuestionApplication` |

## Chạy local

```bash
# Tạo file .env ở thư mục gốc theo mục "Biến môi trường" trong README gốc
docker compose -f ../docker-compose.yml up -d postgres redis kafka minio eureka
mvn spring-boot:run
```

Swagger: `http://localhost:8081/api/swagger-ui.html`

## Cấu trúc package

Kiến trúc **layered**: `controller` → `service` → `repository`.

Gốc: `src/main/java/org/aibles/intellihirequestion/`

| Package | Chứa gì | Ví dụ tên class |
|---|---|---|
| `config/` | Cấu hình Spring (`@Configuration`, `@ConfigurationProperties`) | `RedisConfig`, `OpenApiConfig`, `JpaAuditingConfig` |
| `controller/` | REST controller, chỉ nhận request, validate, gọi service, trả `BaseResponse` | `QuestionController` |
| `dto/request/` | Body/query đầu vào, có annotation validation | `CreateQuestionRequest`, `UpdateQuestionRequest` |
| `dto/response/` | Dữ liệu trả ra client; `BaseResponse<T>`, `PageResponse<T>` dùng chung | `QuestionResponse` |
| `dto/ai/` | DTO gọi / nhận từ LLM | `GenerateQuestionAiRequest` |
| `entity/` | JPA entity, kế thừa `BaseEntity` (id, createdAt, createdBy, updatedAt, updatedBy) | `Question` |
| `entity/enums/` | Enum dùng trong entity | `QuestionStatus` |
| `entity/converter/` | `AttributeConverter` (JSONB, list...) | `StringListConverter` |
| `exception/` | `BaseException`, `ErrorCode` (mã `<PREFIX>_<số>`), `GlobalExceptionHandler` | `NotFoundException`, `ErrorCode.QUE_001` |
| `filter/` | `HeaderAuthFilter` (đọc `X-User-Id`, `X-User-Role` từ gateway, thiếu → 401), `MdcLoggingFilter` (requestId, userId) | `HeaderAuthFilter` |
| `mapper/` | MapStruct `@Mapper(componentModel = "spring")` entity ↔ DTO | `QuestionMapper` |
| `projection/` | Spring Data projection cho query đọc | `QuestionCountProjection` |
| `repository/` | Spring Data JPA repository | `QuestionRepository` |
| `specification/` | `Specification` cho tìm kiếm / lọc động | `QuestionSpecification` |
| `service/` | Interface nghiệp vụ | `QuestionService` |
| `service/impl/` | Hiện thực service, `@Transactional` | `QuestionServiceImpl` |
| `service/ai/` | Interface gọi LLM (`AiProvider`) | `AiProvider` |
| `service/ai/impl/` | Hiện thực theo nhà cung cấp | `OpenRouterAiProvider` |
| `client/` | REST client gọi service khác (WebClient) | `ResumeServiceClient` |
| `utils/` | Hàm tiện ích tĩnh | `PaginationUtils` |

Resources:

| Đường dẫn | Chứa gì |
|---|---|
| `application.yml` | Cấu hình chung, đọc biến môi trường (xem README gốc) |
| `application-dev.yml`, `application-prod.yml` | Ghi đè theo profile |
| `db/migration/` | Flyway: `V1__create_<bảng>.sql`, `V2__...`; không sửa file đã merge |

## Cấu trúc test

Gốc: `src/test/java/org/aibles/intellihirequestion/`

| Package | Loại test |
|---|---|
| `controller/` | `@WebMvcTest` + MockMvc — `QuestionControllerTest` |
| `service/impl/` | Unit test Mockito — `QuestionServiceImplTest` |
| `repository/` | `@DataJpaTest` + Testcontainers PostgreSQL — `QuestionRepositoryTest` |
| `integration/` | `@SpringBootTest` + Testcontainers, luồng đầy đủ — `QuestionFlowIntegrationTest` |

Tên phương thức test: `method_should_expected_when_condition`.

Xem quy ước chung ở [README gốc](../README.md).

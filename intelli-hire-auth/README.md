# intelli-hire-auth

IntelliHire - Xác thực, JWT, OAuth, RBAC. Thiết kế chi tiết: [docs/services/auth-service.md](../docs/services/auth-service.md).

| Thuộc tính | Giá trị |
|---|---|
| Tên service (Eureka) | `auth-service` |
| Port | `8090` |
| Context path | / |
| Schema DB | `auth` |
| Package gốc | `org.aibles.intellihire.authservice` |
| Class main | `AuthServiceApplication` |

## Chạy local

```bash
# Tạo file .env ở thư mục gốc theo mục "Biến môi trường" trong README gốc
docker compose -f ../docker-compose.yml up -d postgres redis kafka minio eureka
mvn spring-boot:run
```

Swagger: `http://localhost:8090/swagger-ui.html`

## Cấu trúc package

Kiến trúc **hexagonal theo feature**: mỗi feature (`account`, `admin`, `auth`, `oauth`, `profile`, `rbac`) có `adapter/web` → `usecase` → `adapter/repository`; phần dùng chung nằm trong `shared/`.

Gốc: `src/main/java/org/aibles/intellihire/authservice/`

| Package | Chứa gì | Ví dụ tên class |
|---|---|---|
| `<feature>/adapter/web/` | REST controller của feature | `AuthController` |
| `<feature>/adapter/web/dto/` | Request/response của API | `LoginRequest`, `TokenResponse` |
| `<feature>/adapter/repository/` | Spring Data repository + entity JPA | `AccountRepository`, `AccountEntity` |
| `<feature>/domain/` | Model / enum nghiệp vụ | `AccountStatus` |
| `<feature>/usecase/` | Interface use case + `Impl` | `LoginUseCase`, `LoginUseCaseImpl` |
| `<feature>/usecase/command/` | Input của use case (record) | `LoginCommand` |
| `shared/config/` | Security, Redis, OpenAPI... | `SecurityConfig` |
| `shared/config/properties/` | `@ConfigurationProperties` có validate | `JwtProperties` |
| `shared/exception/` | `ErrorCode` (`AUTH_<số>`), `GlobalExceptionHandler` | `ErrorCode.AUTH_1001` |
| `shared/dto/` | `BaseResponse<T>`, `PageResponse<T>` | |
| `shared/email/`, `shared/ratelimit/`, `shared/service/`, `shared/util/` | Thành phần dùng chung | `EmailService` |

Resources:

| Đường dẫn | Chứa gì |
|---|---|
| `application.yml` | Cấu hình chung, đọc biến môi trường (xem README gốc) |
| `application-dev.yml`, `application-prod.yml` | Ghi đè theo profile |
| `db/migration/` | Flyway: `V1__create_<bảng>.sql`, `V2__...`; không sửa file đã merge |

## Cấu trúc test

Gốc: `src/test/java/org/aibles/intellihire/authservice/`

| Package | Loại test |
|---|---|
| `account/usecase/` | Unit test use case — `LoginUseCaseImplTest` |
| `admin/usecase/` | Unit test use case — `LoginUseCaseImplTest` |
| `auth/usecase/` | Unit test use case — `LoginUseCaseImplTest` |
| `oauth/usecase/` | Unit test use case — `LoginUseCaseImplTest` |
| `profile/usecase/` | Unit test use case — `LoginUseCaseImplTest` |
| `shared/` | Unit test use case — `LoginUseCaseImplTest` |
| `integration/` | `@SpringBootTest` + Testcontainers, luồng đầy đủ — `QuestionFlowIntegrationTest` |

Tên phương thức test: `method_should_expected_when_condition`.

Xem quy ước chung ở [README gốc](../README.md).

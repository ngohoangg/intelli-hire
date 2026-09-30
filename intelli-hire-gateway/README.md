# intelli-hire-gateway

API Gateway (Spring Cloud Gateway, reactive). Thiết kế: [docs/services/gateway-eureka.md](../docs/services/gateway-eureka.md).

| Thuộc tính | Giá trị |
|---|---|
| Port | `8080` |
| Package gốc | `org.aibles.intellihiregateway` |
| Class main | `IntelliHireGatewayApplication` |

Bảng route đã khai báo trong `application.yml`.

## Cấu trúc package

| Package | Chứa gì | Ví dụ tên class |
|---|---|---|
| `config/` | Cấu hình rate limit, key resolver, bean chung | `RateLimitConfig` |
| `config/properties/` | `@ConfigurationProperties` cho JWT, public paths | `JwtProperties` |
| `filter/` | `GlobalFilter`: xác thực JWT, xóa và gắn lại header `X-User-Id`, `X-User-Role`, `X-User-Name` | `JwtAuthFilter`, `HeaderRelayFilter` |
| `exception/` | Trả lỗi JSON thống nhất cho 401/403/429 | `GatewayErrorHandler` |
| `utils/` | Hàm tiện ích | `JwtUtils` |

Lưu ý: gateway dùng WebFlux, không dùng `spring-boot-starter-web`.

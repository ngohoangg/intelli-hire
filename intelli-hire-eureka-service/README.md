# intelli-hire-eureka-service

Service discovery (Eureka Server). Thiết kế: [docs/services/gateway-eureka.md](../docs/services/gateway-eureka.md).

| Thuộc tính | Giá trị |
|---|---|
| Port | `8761` |
| Package gốc | `org.aibles.intellihireeureka` |
| Class main | `IntelliHireEurekaApplication` |
| Dashboard | `http://localhost:8761` |

Service này chỉ có class main và cấu hình, không cần thêm code.

```bash
mvn spring-boot:run
```

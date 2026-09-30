# IntelliHire – Bộ tài liệu phân tích & thiết kế hệ thống

## Mục lục

| # | Tài liệu | Loại tài liệu | Nội dung chính |
|---|---|---|---|
| 01 | [Giới thiệu](01-gioi-thieu.md) | Tổng quan | Bài toán, mục tiêu, chức năng chính, phạm vi, thuật ngữ, hướng dẫn đọc |
| 02 | [Đặc tả yêu cầu (SRS)](02-dac-ta-yeu-cau.md) | SRS | Tác nhân, yêu cầu chức năng, phi chức năng |
| 03 | [Use case](03-use-case.md) | Phân tích | Sơ đồ use case, đặc tả use case chính |
| 04 | [Kiến trúc tổng thể (HLD)](04-kien-truc-tong-the.md) | HLD | Sơ đồ thành phần, công nghệ, port, giao tiếp |
| 05 | [Thiết kế tích hợp](05-thiet-ke-tich-hop.md) | Interface spec | Kafka event, WebSocket, REST liên service, luồng E2E |
| 06 | [Thiết kế frontend](06-thiet-ke-frontend.md) | UI design | Sitemap, routing, guard, interceptor, service |
| 07 | [Bảo mật](07-bao-mat.md) | Security design | AuthN/AuthZ, JWT, STRIDE, khoảng trống |
| 08 | [Quyết định kiến trúc (ADR)](08-adr.md) | ADR | Các quyết định chính và lý do |
| 09 | [Chiến lược kiểm thử](09-chien-luoc-kiem-thu.md) | Test plan | Cấp độ test, công cụ, test case trọng điểm |
| 10 | [Triển khai & vận hành](10-trien-khai-van-hanh.md) | Deployment / Ops | Môi trường, biến môi trường, giám sát |
| 11 | [Rủi ro & lưu ý khi triển khai](11-rui-ro-trien-khai.md) | Risk register | Rủi ro, ảnh hưởng, biện pháp và thứ tự ưu tiên |

### Thiết kế chi tiết từng service (LLD)

Mỗi file gồm: trách nhiệm · ERD · từ điển dữ liệu · API · sequence · state machine (nếu có) · cấu trúc package · cấu hình · lỗi.

| Service | Tài liệu | Port |
|---|---|---|
| Auth | [services/auth-service.md](services/auth-service.md) | 8090 |
| Question | [services/question-service.md](services/question-service.md) | 8081 |
| Resume | [services/resume-service.md](services/resume-service.md) | 8082 |
| Interview | [services/interview-service.md](services/interview-service.md) | 8083 |
| Grading | [services/grading-service.md](services/grading-service.md) | 8084 |
| Report | [services/report-service.md](services/report-service.md) | 8085 |
| Gateway & Eureka | [services/gateway-eureka.md](services/gateway-eureka.md) | 8080 / 8761 |

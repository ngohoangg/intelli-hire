# IntelliHire – Bộ tài liệu phân tích & thiết kế hệ thống

> Cập nhật: 2026-09-29 · Nguồn: mã nguồn hiện tại của các repo `Aibles-Java/intelli-hire-*`.
> Sơ đồ viết bằng [Mermaid](https://mermaid.js.org/) — xem trực tiếp trên GitHub/GitLab, VS Code (extension Markdown Preview Mermaid) hoặc IntelliJ.

## Mục lục

| # | Tài liệu | Loại tài liệu | Nội dung chính |
|---|---|---|---|
| 01 | [Giới thiệu](01-gioi-thieu.md) | Tổng quan | Mục đích, phạm vi, thuật ngữ, tham chiếu |
| 02 | [Đặc tả yêu cầu (SRS)](02-dac-ta-yeu-cau.md) | SRS | Tác nhân, yêu cầu chức năng, phi chức năng |
| 03 | [Use case](03-use-case.md) | Phân tích | Sơ đồ use case, đặc tả use case chính |
| 04 | [Kiến trúc tổng thể (HLD)](04-kien-truc-tong-the.md) | HLD | Sơ đồ thành phần, công nghệ, port, giao tiếp |
| 05 | [Thiết kế tích hợp](05-thiet-ke-tich-hop.md) | Interface spec | Kafka event, WebSocket, REST liên service, luồng E2E |
| 06 | [Thiết kế frontend](06-thiet-ke-frontend.md) | UI design | Sitemap, routing, guard, interceptor, service |
| 07 | [Bảo mật](07-bao-mat.md) | Security design | AuthN/AuthZ, JWT, STRIDE, khoảng trống |
| 08 | [Quyết định kiến trúc (ADR)](08-adr.md) | ADR | Các quyết định chính và lý do |
| 09 | [Chiến lược kiểm thử](09-chien-luoc-kiem-thu.md) | Test plan | Cấp độ test, công cụ, test case trọng điểm |
| 10 | [Triển khai & vận hành](10-trien-khai-van-hanh.md) | Deployment / Ops | Môi trường, biến môi trường, giám sát |
| 11 | [Vấn đề tồn đọng](11-van-de-ton-dong.md) | Gap analysis | Lỗi/thiếu sót phát hiện khi đọc code |

### Thiết kế chi tiết từng service (LLD)

Mỗi file gồm: trách nhiệm · ERD · từ điển dữ liệu · API · sequence · state machine (nếu có) · cấu trúc package · cấu hình · lỗi.

| Service | Tài liệu | Port | Owner (theo phân công) |
|---|---|---|---|
| Auth | [services/auth-service.md](services/auth-service.md) | 8090 | A |
| Question | [services/question-service.md](services/question-service.md) | 8081 | B |
| Resume | [services/resume-service.md](services/resume-service.md) | 8082 | B |
| Interview | [services/interview-service.md](services/interview-service.md) | 8083 | C |
| Grading | [services/grading-service.md](services/grading-service.md) | 8084 | C |
| Report | [services/report-service.md](services/report-service.md) | 8085 | A |
| Gateway & Eureka | [services/gateway-eureka.md](services/gateway-eureka.md) | 8080 / 8761 | A |

## Quy ước

- Đường dẫn API ghi **đúng như service lắng nghe** (đã tính context-path). Cột "qua FE proxy" cho biết đường dẫn FE gọi.
- Mục **"Hiện trạng"** mô tả code đang có; mục **"Mục tiêu"** là thiết kế đề xuất chưa được hiện thực.
- ⚠️ đánh dấu điểm lệch giữa thiết kế và code — tổng hợp tại [11-van-de-ton-dong.md](11-van-de-ton-dong.md).

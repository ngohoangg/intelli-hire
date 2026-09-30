# 08 – Quyết định kiến trúc (ADR)

Mỗi ADR ghi lại một quyết định, bối cảnh và hệ quả.

| ID | Quyết định |
|---|---|
| ADR-001 | Kiến trúc microservice, database-per-service |
| ADR-002 | Kafka cho chuỗi phỏng vấn → chấm → báo cáo |
| ADR-003 | WebSocket STOMP cho phòng phỏng vấn |
| ADR-004 | JWT HMAC + refresh token xoay vòng lưu DB |
| ADR-005 | Hàng đợi Redis list cho parse CV |
| ADR-006 | Trừu tượng hóa nhà cung cấp LLM |
| ADR-007 | MinIO lưu file CV |
| ADR-008 | Không dùng quan hệ JPA, FK là cột thuần |
| ADR-009 | API Gateway xác thực tập trung, service tin header nội bộ |
| ADR-010 | Chuẩn hóa định danh người dùng là String |

---

## ADR-001 Microservice, database-per-service
- **Bối cảnh:** Nhiều miền được phát triển song song, mỗi miền (auth, CV, câu hỏi, phỏng vấn, chấm, báo cáo) có vòng đời và tải khác nhau; chấm bằng LLM chậm và tốn tài nguyên.
- **Quyết định:** Mỗi miền một service Spring Boot, một schema PostgreSQL riêng, Flyway riêng; không truy vấn chéo schema.
- **Hệ quả:** (+) Tách sở hữu, deploy độc lập, scale grading riêng. (−) Cần hợp đồng API/sự kiện, dữ liệu phân tán (tên câu hỏi được sao chép vào `question_logs`, `grading_results`, `question_scores`), vận hành phức tạp hơn.

## ADR-002 Kafka cho luồng hậu phỏng vấn
- **Bối cảnh:** Chấm 1 phiên cần N lời gọi LLM (mỗi lời ≤ 30 s); không thể giữ request của người dùng.
- **Quyết định:** interviews phát `session_completed`; grading consume, chấm, phát `grading_completed`; report consume.
- **Phương án khác:** gọi REST đồng bộ (bị chặn lâu, lỗi dây chuyền); Redis pub/sub (không bền).
- **Hệ quả:** (+) Tách thời gian, replay được, thêm consumer mới dễ. (−) Cần idempotency, DLT, theo dõi lag; FE phải polling kết quả.

## ADR-003 WebSocket STOMP
- **Bối cảnh:** Hỏi–đáp tuần tự, cần server đẩy câu kế tiếp ngay.
- **Quyết định:** Spring WebSocket + STOMP + SockJS, simple broker `/topic`.
- **Hệ quả:** (+) Đơn giản, fallback SockJS. (−) Simple broker nằm trong bộ nhớ → khi scale >1 instance cần sticky session hoặc broker relay (RabbitMQ); cần xác thực ở CONNECT.

## ADR-004 JWT HMAC + refresh token trong DB
- **Quyết định:** Access token ngắn hạn stateless mang `roles`, `permissions`; refresh token có `jti` lưu DB, xoay vòng mỗi lần dùng; Redis theo dõi idle.
- **Hệ quả:** (+) Service khác verify offline; thu hồi được phiên. (−) Secret HMAC phải chia sẻ cho mọi bên verify (gateway); cân nhắc RS256 + JWKS nếu nhiều bên verify.

## ADR-005 Redis list làm hàng đợi parse CV
- **Quyết định:** Sau commit upload, `LPUSH resumeId` vào `resume:parse:queue`; scheduler 5 s `RPOP` và giao cho thread pool.
- **Phương án khác:** Kafka (nặng cho một luồng nội bộ), `@Async` trực tiếp (mất job khi restart).
- **Hệ quả:** (+) Nhẹ, dùng hạ tầng có sẵn. (−) `RPOP` mất job nếu worker chết giữa chừng; mỗi tick chỉ lấy 1 job. Cải tiến: `BLMOVE` sang danh sách processing, hoặc quét lại job `QUEUED` quá hạn.

## ADR-006 Trừu tượng hóa LLM
- **Quyết định:** Interface `AiProvider` (resume) với 5 hiện thực chọn bằng `ai.provider`; grading và question dùng OpenRouter.
- **Hệ quả:** (+) Đổi nhà cung cấp theo chi phí/chất lượng. (−) Ba service có ba client khác nhau; nên gom thành module `ai-client` dùng chung.

## ADR-007 MinIO
- **Quyết định:** Lưu file CV trên MinIO (API S3), DB chỉ giữ metadata (`object_bucket`, `object_key`, `etag`).
- **Hệ quả:** (+) Không phình DB, chuyển sang S3 thật dễ. (−) Thêm một thành phần hạ tầng; xóa resume cần dọn object.

## ADR-008 Không dùng quan hệ JPA
- **Quyết định:** Entity lưu FK dạng cột thuần; ràng buộc FK đặt trong Flyway SQL.
- **Hệ quả:** (+) Tránh N+1 và lazy-loading ngoài ý muốn, entity đơn giản. (−) Truy vấn ghép phải viết tay; tính toàn vẹn dựa vào DB.

## ADR-009 API Gateway
- **Bối cảnh:** 5/6 service tin header `X-User-*` mà client tự gửi được.
- **Quyết định:** Spring Cloud Gateway verify JWT, xóa header `X-User-*` từ client và gắn lại từ claim; service chỉ lắng nghe trong mạng nội bộ.
- **Hệ quả:** Service không cần Spring Security đầy đủ; phải đảm bảo không có đường vào vòng qua gateway. Chi tiết: [services/gateway-eureka.md](services/gateway-eureka.md).

## ADR-010 Kiểu định danh người dùng
- **Bối cảnh:** auth dùng BIGINT, interviews/grading dùng String, report dùng BIGINT, resume dùng VARCHAR(36).
- **Quyết định:** Mọi service lưu `user_id` dạng `VARCHAR(64)` chứa `sub` của JWT.
- **Hệ quả:** Cần migration cho report (`user_id`, `user_progress.user_id`).

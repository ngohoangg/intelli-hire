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
- **Quyết định:** Spring WebSocket + STOMP + SockJS, simple broker với user destination `/user/queue/interview`; xác thực JWT ở CONNECT.
- **Hệ quả:** (+) Đơn giản, fallback SockJS. (−) Simple broker nằm trong bộ nhớ → khi scale >1 instance cần sticky session hoặc broker relay (RabbitMQ).

## ADR-004 JWT HMAC + refresh token trong DB

- **Quyết định:** Access token ngắn hạn stateless mang `roles`, `permissions`; refresh token có `jti` lưu DB, xoay vòng mỗi lần dùng; Redis theo dõi idle.
- **Hệ quả:** (+) Service khác verify offline; thu hồi được phiên. (−) Secret HMAC phải chia sẻ cho mọi bên verify (gateway); cân nhắc RS256 + JWKS nếu nhiều bên verify.

## ADR-005 Redis list làm hàng đợi parse CV

- **Quyết định:** Sau commit upload, `LPUSH resumeId` vào `resume:parse:queue`; scheduler 5 s `RPOP` và giao cho thread pool.
- **Phương án khác:** Kafka (nặng cho một luồng nội bộ), `@Async` trực tiếp (mất job khi restart).
- **Hệ quả:** (+) Nhẹ, dùng hạ tầng có sẵn. (−) `RPOP` mất job nếu worker chết giữa chừng; mỗi tick chỉ lấy 1 job. Giảm thiểu: `BLMOVE` sang danh sách processing, quét lại job `QUEUED` quá hạn.

## ADR-006 Trừu tượng hóa LLM

- **Quyết định:** Module `ai-client` dùng chung, định nghĩa interface `AiProvider` với 5 implementation (OpenAI, Anthropic Claude, Google Gemini, xAI Grok, OpenRouter) chọn bằng cấu hình `ai.provider`; resume, question và grading cùng dùng module này.
- **Hệ quả:** (+) Đổi nhà cung cấp theo chi phí/chất lượng mà không sửa service; retry, timeout, xử lý lỗi thống nhất. (−) Prompt và định dạng phản hồi cần kiểm thử trên từng nhà cung cấp.

## ADR-007 MinIO

- **Quyết định:** Lưu file CV trên MinIO (API S3), DB chỉ giữ metadata (`object_bucket`, `object_key`, `etag`).
- **Hệ quả:** (+) Không phình DB, chuyển sang S3 thật dễ. (−) Thêm một thành phần hạ tầng; xóa resume cần dọn object.

## ADR-008 Không dùng quan hệ JPA

- **Quyết định:** Entity lưu FK dạng cột thuần; ràng buộc FK đặt trong Flyway SQL.
- **Hệ quả:** (+) Tránh N+1 và lazy-loading ngoài ý muốn, entity đơn giản. (−) Truy vấn ghép phải viết tay; tính toàn vẹn dựa vào DB.

## ADR-009 API Gateway

- **Bối cảnh:** Nếu mỗi service tự xác thực JWT thì trùng lặp cấu hình bảo mật; nếu service tin header `X-User-*` do client gửi thì có thể bị giả mạo danh tính.
- **Quyết định:** Spring Cloud Gateway verify JWT, xóa header `X-User-*` từ client và gắn lại từ claim; service chỉ lắng nghe trong mạng nội bộ.
- **Hệ quả:** Service không cần Spring Security đầy đủ; phải đảm bảo không có đường vào vòng qua gateway. Chi tiết: [services/gateway-eureka.md](services/gateway-eureka.md).

## ADR-010 Kiểu định danh người dùng

- **Bối cảnh:** Mọi service đều tham chiếu người dùng; nếu mỗi service chọn một kiểu (BIGINT, UUID, VARCHAR) sẽ phát sinh lỗi parse và sai lệch khi đối chiếu dữ liệu.
- **Quyết định:** Mọi service lưu `user_id` dạng `VARCHAR(64)` chứa `sub` của JWT.
- **Hệ quả:** (+) Không phụ thuộc cách auth sinh id; sự kiện Kafka và header dùng chung một kiểu. (−) Không đặt được FK tới bảng account (đã chấp nhận theo ADR-001).

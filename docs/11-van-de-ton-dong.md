# 11 – Vấn đề tồn đọng (gap analysis)

Danh sách lệch giữa thiết kế và code phát hiện khi đọc mã nguồn (2026-09-29), xếp theo mức độ. Các mục đánh dấu **Chặn** làm hỏng luồng nghiệp vụ chính hoặc lộ dữ liệu.

| # | Mức | Service | Vấn đề | Ảnh hưởng | Đề xuất |
|---|---|---|---|---|---|
| 1 | Chặn | resume, report, grading, question | Service tin header `X-User-*`; resume gán `user-001` khi thiếu; report không có security | Ai cũng đọc/sửa dữ liệu người khác | Gateway + kiểm sở hữu ([07](07-bao-mat.md)) |
| 2 | Chặn | interviews | `HeaderAuthFilter` bị tắt → `userId` null | Phiên không gắn đúng người dùng | Bật lại filter |
| 3 | Chặn | interviews | `createSession` gửi `topic_id=null`, `difficulty_id=null` | Câu hỏi không theo topic/độ khó đã chọn | FE gửi id, hoặc tra id theo tên |
| 4 | Chặn | grading → report | `skill_name` luôn null | Không có điểm kỹ năng, tiến bộ, top/weak skills | Dùng topic/tag làm skill |
| 5 | Cao | grading | Không dùng rubric; `max_score` cố định 10 | Chấm kém chính xác, bỏ qua dữ liệu đã nhập | Lấy rubric từ question service khi chấm |
| 6 | Cao | question | Endpoint admin/moderation không kiểm role | Người dùng tự duyệt câu hỏi | Kiểm role ADMIN |
| 7 | Cao | auth | OAuth callback không kiểm `state`, không tạo key `session:activity` | CSRF đăng nhập; token OAuth bị 401 | Lưu `state` Redis; tạo activity key |
| 8 | Cao | report | Consumer nuốt lỗi, không retry/DLT | Mất báo cáo | Retry + DLT như grading |
| 9 | Cao | frontend | Dev bật mock parse CV và mock interview | Không kiểm được luồng thật; trái quy tắc "chỉ dùng API thật" | Sửa backend, tắt cờ, xóa `core/mocks` |
| 10 | Trung bình | interviews | WS `/topic/interview/{id}` không kiểm sở hữu, origins `*` | Nghe lén phiên người khác | User destination + JWT ở CONNECT |
| 11 | Trung bình | resume | AI review chỉ chạy với OpenRouter | Lỗi khi đổi provider | Hiện thực `complete()` cho các provider |
| 12 | Trung bình | auth | Đổi mật khẩu không thu hồi phiên khác; không tự mở khóa | Lệch tài liệu; người dùng bị khóa vĩnh viễn | Revoke khi đổi; mở khóa sau `PASSWORD_LOCK_DURATION_MINUTES` |
| 13 | Trung bình | question | `status` entity mặc định PENDING, DB mặc định APPROVED; cần xác minh `/random` chỉ lấy APPROVED | Câu chưa duyệt lọt vào phỏng vấn | Thống nhất default, lọc trạng thái |
| 14 | Trung bình | grading | Chấm cả phiên `EXPIRED` | Tốn chi phí LLM cho phiên bỏ dở | Quyết định nghiệp vụ; lọc nếu cần |
| 15 | Trung bình | toàn hệ thống | Kiểu `user_id` không thống nhất (BIGINT/String) | Lỗi parse, join sai | ADR-010 |
| 16 | Thấp | question, resume | Port mặc định 8080 trùng nhau | Xung đột khi chạy local | Đặt default 8081/8082 |
| 17 | Thấp | toàn hệ thống | JSON snake_case (interviews, rubric) vs camelCase (còn lại) | FE phải xử lý 2 kiểu | Chốt một quy ước |
| 18 | Thấp | interviews | `answers_submitted` không có consumer | Tài nguyên thừa | Dùng cho chấm dần hoặc bỏ |
| 19 | Thấp | auth | `/resend-verification` permit nhưng chưa có API | 404 | Hiện thực hoặc bỏ |
| 20 | Thấp | repo | `intelli-hire-question/` chứa bản sao `intelli-hire-grading/`, `intelli-hire-interviews/` | Nhầm lẫn khi sửa code | Xóa bản sao |
| 21 | Thấp | gateway, eureka | Chưa có code | FE phụ thuộc dev proxy | [services/gateway-eureka.md](services/gateway-eureka.md) |
| 22 | Thấp | resume | Chưa có `Dockerfile` | Không build được bằng compose chung | Bổ sung |

## Thứ tự xử lý đề xuất

1. **Ưu tiên cao nhất:** #2, #3, #4 (luồng phỏng vấn → báo cáo chạy đúng), #9 (tắt mock).
2. **Tiếp theo:** #1 + #21 (gateway), #6, #7, #10 (bảo mật).
3. **Sau đó:** #5, #8, #11–#15.
4. **Dọn dẹp:** #16–#20, #22.

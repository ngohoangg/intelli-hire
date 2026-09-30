# 11 – Rủi ro & lưu ý khi triển khai

Danh sách rủi ro cần lưu ý khi hiện thực hệ thống theo thiết kế, xếp theo mức độ. Mức **Cao** là các rủi ro có thể làm hỏng luồng nghiệp vụ chính hoặc lộ dữ liệu người dùng.

## 11.1 Danh sách rủi ro

| # | Mức | Phạm vi | Rủi ro | Ảnh hưởng | Biện pháp |
|---|---|---|---|---|---|
| 1 | Cao | resume, report, grading, question, interviews | Service tin header `X-User-*` do client tự gửi | Giả mạo danh tính, đọc/sửa dữ liệu người khác | Gateway ghi đè header; service chỉ nhận request từ mạng nội bộ, thiếu `X-User-Id` → 401; kiểm quyền sở hữu ([07](07-bao-mat.md)) |
| 2 | Cao | interviews → question | Tạo phiên không truyền đúng `topic_id`, `difficulty_id` | Câu hỏi không theo topic/độ khó người dùng chọn | Truyền id theo lựa chọn khi tạo phiên; test tích hợp TC-INT |
| 3 | Cao | grading → report | Kết quả chấm thiếu `skillName` | Không tính được điểm kỹ năng, tiến bộ, top/weak skills | Lấy skill từ topic/tag của câu hỏi; contract test cho sự kiện `grading_completed` |
| 4 | Cao | grading | Chấm không theo rubric | Điểm thiếu căn cứ, bỏ phí dữ liệu rubric | Grading lấy rubric từ question service; `maxScore` theo rubric |
| 5 | Cao | question | Endpoint admin/moderation thiếu kiểm role | Người dùng tự duyệt câu hỏi của mình | Kiểm role `ADMIN`; test phân quyền TC-AUTH-06 tương tự cho question |
| 6 | Cao | auth | OAuth callback không kiểm `state` | CSRF đăng nhập | Lưu `state` trong Redis (TTL ngắn), so khớp ở callback; tạo key `session:activity` khi đăng nhập OAuth |
| 7 | Cao | report | Consumer `grading_completed` lỗi mà không retry | Mất báo cáo của phiên | Retry có giới hạn + dead-letter topic như grading |
| 8 | Trung bình | interviews | Kênh WebSocket không gắn với người dùng | Nghe lén phiên phỏng vấn người khác | User destination `/user/queue/interview`, JWT ở STOMP CONNECT, giới hạn allowed origins |
| 9 | Trung bình | LLM | Nhà cung cấp LLM chậm, lỗi, giới hạn tần suất (429) hoặc trả JSON sai định dạng | Chấm/parse CV thất bại, trải nghiệm chậm | Timeout, retry có backoff, trạng thái `FAILED` để chạy lại; parse JSON chịu lỗi |
| 10 | Trung bình | LLM | Chi phí LLM tăng do spam sinh câu hỏi, review CV, tạo phiên | Vượt ngân sách | Rate limit theo user ở gateway, hạn mức mỗi ngày, cảnh báo chi phí |
| 11 | Trung bình | LLM | Prompt injection trong câu trả lời hoặc CV | Điểm bị thao túng | Bọc dữ liệu bằng delimiter, dặn bỏ qua chỉ thị trong dữ liệu; test TC-GRA-07 |
| 12 | Trung bình | resume | Hỗ trợ nhiều nhà cung cấp LLM nhưng prompt chỉ kiểm thử trên một nhà cung cấp | Lỗi khi đổi provider | Module `ai-client` dùng chung (ADR-006), test với từng provider |
| 13 | Trung bình | auth | Đổi mật khẩu không thu hồi các phiên khác; tài khoản bị khóa không tự mở | Phiên cũ vẫn dùng được; người dùng bị khóa vĩnh viễn | Thu hồi refresh token khi đổi mật khẩu; tự mở khóa sau `PASSWORD_LOCK_DURATION_MINUTES` |
| 14 | Trung bình | question | Trạng thái mặc định của câu hỏi không thống nhất giữa entity và DB | Câu chưa duyệt lọt vào phỏng vấn | Mặc định `PENDING` ở cả hai; `/random` chỉ lấy `APPROVED` |
| 15 | Trung bình | grading | Chấm cả phiên `EXPIRED` | Tốn chi phí LLM cho phiên bỏ dở | Chốt quy tắc nghiệp vụ: chỉ chấm các câu đã trả lời, bỏ qua phiên không có câu trả lời |
| 16 | Trung bình | toàn hệ thống | Kiểu `user_id` và `sessionId` không thống nhất giữa service/sự kiện | Lỗi parse, join sai | `user_id` là String (ADR-010), `sessionId` là Long ở mọi sự kiện |
| 17 | Thấp | toàn hệ thống | JSON trộn `snake_case` và `camelCase` | Frontend phải xử lý 2 kiểu | Dùng `camelCase` cho mọi service |
| 18 | Thấp | resume | Worker parse CV dừng giữa chừng làm mất job trong hàng đợi Redis | Job kẹt ở `QUEUED` | `BLMOVE` sang danh sách processing, quét lại job quá hạn (ADR-005) |
| 19 | Thấp | interviews | Simple broker nằm trong bộ nhớ | Không scale ngang được kết nối WebSocket | Sticky session hoặc broker relay khi chạy > 1 instance (ADR-003) |
| 20 | Thấp | toàn hệ thống | Cấu hình port, tên biến môi trường không đồng nhất | Xung đột khi chạy local, sai cấu hình khi deploy | Cố định port 8081–8085, 8090; dùng chung bộ tên biến ([10](10-trien-khai-van-hanh.md)) |
| 21 | Thấp | gateway | Ở dev frontend gọi thẳng service qua proxy, khác đường đi qua gateway ở production | Lỗi định tuyến/xác thực chỉ lộ ra ở staging | Triển khai gateway sớm ([services/gateway-eureka.md](services/gateway-eureka.md)) |

## 11.2 Thứ tự ưu tiên khi triển khai

1. **Ưu tiên cao nhất:** #2, #3, #4 để luồng phỏng vấn → chấm → báo cáo chạy đúng.
2. **Tiếp theo:** #1, #21 (gateway), #5, #6, #8 (bảo mật).
3. **Sau đó:** #7, #9–#16.
4. **Hoàn thiện:** #17–#20.

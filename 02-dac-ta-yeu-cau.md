# 02 – Đặc tả yêu cầu phần mềm (SRS)

Yêu cầu được đánh mã `FR-<module>-<số>` / `NFR-<số>` để truy vết sang API ([services/](services/)) và test case ([09](09-chien-luoc-kiem-thu.md)).

## 2.1 Tác nhân

| Tác nhân | Loại | Mô tả |
|---|---|---|
| Khách (Guest) | Người | Chưa đăng nhập: đăng ký, đăng nhập, quên mật khẩu, xác thực email |
| Candidate (`USER`) | Người | Quản lý CV, gửi câu hỏi, phỏng vấn, xem kết quả và báo cáo |
| Admin (`ADMIN`, `SUPER_ADMIN`) | Người | Quản trị tài khoản, role, permission; kiểm duyệt và quản lý câu hỏi |
| LLM provider | Hệ thống ngoài | Parse CV, review CV, sinh câu hỏi, chấm điểm |
| SMTP server | Hệ thống ngoài | Gửi email xác thực và link đặt lại mật khẩu |
| OAuth provider | Hệ thống ngoài | Google, GitHub |
| Scheduler | Hệ thống nội bộ | Hết hạn phiên phỏng vấn, lấy job parse CV từ hàng đợi |

## 2.2 Yêu cầu chức năng

### Auth
| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-AUTH-01 | Đăng ký tài khoản; gửi link xác thực email (hiệu lực 24h) | Must |
| FR-AUTH-02 | Đăng nhập bằng email hoặc username; nhận access + refresh token | Must |
| FR-AUTH-03 | Làm mới token (rotation), đăng xuất một phiên / tất cả phiên | Must |
| FR-AUTH-04 | Quên mật khẩu: gửi link đặt lại (hiệu lực 1h), đặt lại mật khẩu | Must |
| FR-AUTH-05 | Khóa tài khoản sau N lần đăng nhập sai liên tiếp | Must |
| FR-AUTH-06 | Đăng nhập / đăng ký bằng Google, GitHub; liên kết và hủy liên kết | Should |
| FR-AUTH-07 | Xem/sửa hồ sơ cá nhân; đổi mật khẩu; xóa tài khoản | Must |
| FR-AUTH-08 | Xem danh sách phiên đăng nhập, thu hồi từng phiên | Should |
| FR-AUTH-09 | Admin: danh sách account, khóa/mở khóa, gán/bỏ role | Must |
| FR-AUTH-10 | Admin: CRUD role, xem permission | Must |
| FR-AUTH-11 | Tự đăng xuất khi không hoạt động 30 phút | Should |

### Question
| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-QUE-01 | CRUD câu hỏi (technical / behavioral / coding) gắn topic, độ khó, tag | Must |
| FR-QUE-02 | Rubric 1–1 cho câu hỏi | Must |
| FR-QUE-03 | Tìm kiếm theo từ khóa, lọc, phân trang, đếm câu hỏi | Must |
| FR-QUE-04 | Lấy ngẫu nhiên N câu theo topic/độ khó/loại/tag, loại trừ id đã dùng | Must |
| FR-QUE-05 | Quản lý topic dạng cây, tag, độ khó | Must |
| FR-QUE-06 | Người dùng gửi câu hỏi → chờ duyệt; gửi lại khi bị từ chối | Should |
| FR-QUE-07 | Admin kiểm duyệt: APPROVED / REJECTED (kèm lý do) / ARCHIVED | Should |
| FR-QUE-08 | Nhập hàng loạt câu hỏi | Could |
| FR-QUE-09 | Sinh câu hỏi bằng AI dựa trên CV | Could |
| FR-QUE-10 | Bài học tham khảo (study article) theo topic, độ khó | Could |

### Resume
| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-RES-01 | CRUD resume (tiêu đề duy nhất theo người dùng), xóa mềm | Must |
| FR-RES-02 | CRUD liên hệ, học vấn, kinh nghiệm, kỹ năng của resume | Must |
| FR-RES-03 | Upload 1 file PDF/DOCX ≤ 5MB mỗi resume; tải xuống; xóa | Must |
| FR-RES-04 | Tự động parse CV bất đồng bộ sau upload; theo dõi tiến độ; hủy; chạy lại | Should |
| FR-RES-05 | Sinh / sửa skill profile (top skill, số năm, seniority) | Should |
| FR-RES-06 | AI review CV: điểm tổng 0–10, điểm từng phần, điểm mạnh/yếu, việc ưu tiên | Should |
| FR-RES-07 | Danh mục kỹ năng dùng chung, tìm kiếm | Must |
| FR-RES-08 | Cung cấp hồ sơ tổng hợp cho question service | Should |

### Interview
| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-INT-01 | Tạo phiên: topic, độ khó, số câu (1–20) | Must |
| FR-INT-02 | Nhận từng câu hỏi và gửi câu trả lời qua WebSocket | Must |
| FR-INT-03 | Gửi câu trả lời qua REST (dự phòng) | Should |
| FR-INT-04 | Kết thúc phiên thủ công hoặc tự động khi hết câu | Must |
| FR-INT-05 | Hết hạn phiên sau 60 phút | Must |
| FR-INT-06 | Danh sách phiên, chi tiết phiên (câu hỏi + trả lời), xóa phiên | Must |

### Grading
| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-GRA-01 | Tự chấm khi phiên kết thúc (qua Kafka) | Must |
| FR-GRA-02 | Chấm từng câu bằng LLM: điểm 0..maxScore + nhận xét | Must |
| FR-GRA-03 | Xem kết quả chấm theo id / theo phiên; lịch sử chấm | Must |
| FR-GRA-04 | Chấm lại khi lượt trước thất bại | Should |
| FR-GRA-05 | Admin xem toàn bộ lượt chấm | Could |

### Report
| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-REP-01 | Tạo báo cáo khi nhận kết quả chấm | Must |
| FR-REP-02 | Báo cáo phiên: tổng điểm, điểm từng câu, điểm theo kỹ năng | Must |
| FR-REP-03 | Lịch sử báo cáo, thống kê (TB/cao/thấp, top 3 mạnh/yếu) | Must |
| FR-REP-04 | Tiến bộ theo kỹ năng: điểm gần nhất, trung bình, xu hướng 50 lần | Should |
| FR-REP-05 | Cập nhật tóm tắt / roadmap học tập | Could |

## 2.3 Yêu cầu phi chức năng

| Mã | Nhóm | Yêu cầu | Cách kiểm |
|---|---|---|---|
| NFR-01 | Hiệu năng | API đọc p95 < 300 ms, API ghi p95 < 800 ms (không tính LLM) | Load test (k6/JMeter) |
| NFR-02 | Hiệu năng | WebSocket: gửi trả lời → nhận câu kế tiếp < 500 ms | Load test WS |
| NFR-03 | Hiệu năng | Chấm xong phiên 10 câu < 60 s | Đo `session_completed` → `grading_completed` |
| NFR-04 | Hiệu năng | Parse CV hoàn tất < 2 phút (ước lượng của `JobType.PARSE`) | Metric `resume.parse.duration` |
| NFR-05 | Tin cậy | Sự kiện Kafka không mất khi consumer tạm dừng; xử lý idempotent theo `sessionId` | Test dừng/khởi động consumer |
| NFR-06 | Tin cậy | Lỗi một service không kéo sập service khác (timeout 5–30 s, retry có giới hạn) | Chaos test đơn giản |
| NFR-07 | Bảo mật | Mật khẩu BCrypt(12); token email lưu dạng SHA-256; không log token/mật khẩu | Review + SAST |
| NFR-08 | Bảo mật | Mọi API không public yêu cầu JWT hợp lệ; kiểm tra quyền sở hữu dữ liệu | Test phân quyền |
| NFR-09 | Bảo mật | Secret chỉ qua biến môi trường; không commit `.env` | Secret scan (gitleaks) |
| NFR-10 | Mở rộng | Service stateless (trừ kết nối WS), scale ngang được | Chạy 2 instance |
| NFR-11 | Bảo trì | Mỗi service 1 schema, migration Flyway; OpenAPI tự sinh (springdoc) | CI |
| NFR-12 | Kiểm thử | Coverage service layer ≥ 70% | JaCoCo |
| NFR-13 | Quan sát | Actuator health/metrics/prometheus; log có correlation id (MDC) | Kiểm tra endpoint |
| NFR-14 | Khả dụng | UI responsive, tôn trọng `prefers-reduced-motion` | Review UI |

> Các ngưỡng số của NFR-01 → NFR-03 là mục tiêu đề xuất, cần nhóm chốt.

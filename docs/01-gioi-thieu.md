# 01 – Giới thiệu

IntelliHire là nền tảng luyện phỏng vấn kỹ thuật có AI: ứng viên quản lý CV, làm bài phỏng vấn thời gian thực, được LLM chấm điểm và xem báo cáo tiến bộ theo kỹ năng.

## 1.1 Mục đích tài liệu

- Làm nguồn tham chiếu chung cho nhóm khi phát triển, review và tích hợp.
- Chốt các hợp đồng giữa service: REST API, sự kiện Kafka, giao thức WebSocket, JWT.
- Làm cơ sở cho kiểm thử, triển khai và bảo trì.

## 1.2 Phạm vi

| Thành phần | Repo | Công nghệ |
|---|---|---|
| Auth service | `intelli-hire-auth` | Spring Boot 3.4.2, Java 21 |
| Question service | `intelli-hire-question` | Spring Boot 3.2.1, Java 17 |
| Resume service | `intelli-hire-resume` | Spring Boot 3.2.1, Java 17 |
| Interview service | `intelli-hire-interviews` | Spring Boot 3.2.4, Java 17 |
| Grading service | `intelli-hire-grading` | Spring Boot 3.2.1, Java 17 |
| Report service | `intelli-hire-report` | Spring Boot 3.2.1, Java 17 |
| API Gateway | `intelli-hire-gateway` | Spring Cloud Gateway |
| Service discovery | `intelli-hire-eureka-service` | Eureka Server |
| Frontend | `intelli-hire-uiux/intelli-hire-frontend` | Angular 19, Angular Material |

**Ngoài phạm vi:** thanh toán, tuyển dụng phía doanh nghiệp (đăng tin, pipeline ứng viên), ứng dụng mobile.

## 1.3 Thuật ngữ

| Thuật ngữ | Nghĩa |
|---|---|
| Candidate / USER | Người dùng luyện phỏng vấn; role mặc định `USER` |
| Admin | Role `ADMIN` hoặc `SUPER_ADMIN`; quản trị tài khoản, phân quyền, kiểm duyệt câu hỏi |
| Interview session | Một lượt phỏng vấn gồm N câu hỏi (1–20, mặc định 5) theo topic và độ khó |
| Question log | Bản ghi câu hỏi đã được đưa vào một phiên, có thứ tự |
| Rubric | Tiêu chí chấm gắn 1–1 với câu hỏi (criteria, keywords, lỗi thường gặp, đáp án mẫu, điểm tối đa) |
| Moderation | Quy trình kiểm duyệt câu hỏi do người dùng gửi: PENDING → APPROVED / REJECTED |
| Parse job | Tác vụ bất đồng bộ trích xuất CV (PDF/DOCX) thành dữ liệu có cấu trúc bằng AI |
| Skill profile | Tổng hợp kỹ năng nổi bật, số năm kinh nghiệm, seniority từ CV |
| Grading session / result | Một lượt chấm cho một phiên phỏng vấn / điểm của từng câu |
| Report | Báo cáo tổng hợp điểm của một phiên + tiến bộ theo kỹ năng |
| LLM provider | Nhà cung cấp mô hình ngôn ngữ: OpenAI, Anthropic Claude, Google Gemini, xAI Grok, OpenRouter |

## 1.4 Tài liệu tham chiếu

- `intelli-hire-question/QUESTION_SERVICE_BRD_TDD.md` – BRD/TDD của question service
- `intelli-hire-report/API_DOCUMENTATION.md` – API report service
- `intelli-hire-auth/CODE_REVIEW.md` – kết quả review auth
- `intelli-hire-resume/ROADMAP.md`, `CHECKLIST.md`
- Postman: `intelli-hire-grading/IntelliHire-Full-System.postman_collection.json`, `intelli-hire-report/intelli-hire-report.postman_collection.json`
- `intelli-hire-uiux/intelli-hire-frontend/docs/*-wireframe.md` – wireframe màn câu hỏi

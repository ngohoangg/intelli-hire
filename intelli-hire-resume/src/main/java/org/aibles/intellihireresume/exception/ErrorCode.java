package org.aibles.intellihireresume.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Resume errors
    RES_001("RES_001", "Không tìm thấy hồ sơ"),
    RES_002("RES_002", "Hồ sơ đã tồn tại"),
    RES_003("RES_003", "Trạng thái hồ sơ không hợp lệ"),
    RES_004("RES_004", "Tiêu đề hồ sơ đã tồn tại"),
    RES_005("RES_005", "Không có quyền truy cập hồ sơ"),

    // Common errors
    COM_001("COM_001", "Lỗi xác thực dữ liệu"),
    COM_002("COM_002", "Lỗi hệ thống"),
    COM_003("COM_003", "Yêu cầu không hợp lệ"),
    COM_004("COM_004", "Thiếu header bắt buộc"),

    // Parse job errors
    JOB_001("JOB_001", "Không tìm thấy tác vụ phân tích"),
    JOB_002("JOB_002", "Không thể hủy tác vụ ở trạng thái hiện tại"),
    JOB_003("JOB_003", "Đã có tác vụ đang chạy cho hồ sơ này"),

    // File errors
    FILE_001("FILE_001", "Không tìm thấy tệp"),
    FILE_002("FILE_002", "Loại tệp không hợp lệ, chỉ chấp nhận PDF và DOCX"),
    FILE_003("FILE_003", "Kích thước tệp vượt quá giới hạn 5MB"),
    FILE_004("FILE_004", "Tệp đã tồn tại cho hồ sơ này"),

    // Contact errors
    CONTACT_001("CONTACT_001", "Không tìm thấy thông tin liên hệ"),
    CONTACT_002("CONTACT_002", "Thông tin liên hệ không hợp lệ"),

    // Experience errors
    EXP_001("EXP_001", "Không tìm thấy kinh nghiệm làm việc"),
    EXP_002("EXP_002", "Khoảng thời gian không hợp lệ"),
    EXP_003("EXP_003", "Kinh nghiệm làm việc không thuộc về hồ sơ này"),

    // Education errors
    EDU_001("EDU_001", "Không tìm thấy học vấn"),
    EDU_002("EDU_002", "Khoảng năm không hợp lệ"),
    EDU_003("EDU_003", "Học vấn không thuộc về hồ sơ này"),

    // Skill catalog errors
    SKILL_001("SKILL_001", "Không tìm thấy kỹ năng"),

    // Resume skill errors
    RESUME_SKILL_001("RESUME_SKILL_001", "Không tìm thấy kỹ năng của hồ sơ"),
    RESUME_SKILL_002("RESUME_SKILL_002", "Kỹ năng đã được thêm vào hồ sơ"),

    // Skill profile errors
    PROFILE_001("PROFILE_001", "Không tìm thấy hồ sơ kỹ năng"),

    // AI provider errors
    AI_001("AI_001", "Xác thực nhà cung cấp AI thất bại"),
    AI_002("AI_002", "Yêu cầu tới nhà cung cấp AI thất bại"),
    AI_003("AI_003", "Phân tích AI thất bại sau số lần thử lại tối đa"),

    // Review errors
    REVIEW_001("REVIEW_001", "Không tìm thấy đánh giá hồ sơ"),
    REVIEW_002("REVIEW_002", "Không thể tạo đánh giá: chưa tải lên tệp CV"),
    REVIEW_003("REVIEW_003", "Không thể phân tích phản hồi đánh giá từ AI");

    private final String code;
    private final String message;
}

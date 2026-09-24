package org.aibles.intellihireinterview.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Interview session errors
    INT_001("INT_001", "Không tìm thấy phiên phỏng vấn"),
    INT_002("INT_002", "Phiên phỏng vấn không hoạt động"),
    INT_003("INT_003", "Câu hỏi này đã được trả lời"),
    INT_004("INT_004", "Không có câu hỏi nào cho chủ đề này. Vui lòng thử chủ đề hoặc độ khó khác."),

    // Common errors
    COM_001("COM_001", "Lỗi xác thực dữ liệu"),
    COM_002("COM_002", "Lỗi hệ thống"),
    COM_003("COM_003", "Yêu cầu không hợp lệ"),
    COM_004("COM_004", "Thiếu tiêu đề bắt buộc: X-User-Id");

    private final String code;
    private final String message;
}

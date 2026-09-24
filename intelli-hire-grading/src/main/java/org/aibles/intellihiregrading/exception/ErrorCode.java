package org.aibles.intellihiregrading.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Grading errors
    GRD_001("GRD_001", "Không tìm thấy phiên chấm điểm"),
    GRD_002("GRD_002", "Đã tồn tại chấm điểm cho phiên này"),
    GRD_003("GRD_003", "Mã phiên không hợp lệ"),
    GRD_004("GRD_004", "Trạng thái chấm điểm không hợp lệ"),
    GRD_005("GRD_005", "Không có quyền truy cập, cần vai trò quản trị viên"),
    GRD_006("GRD_006", "Dịch vụ LLM không khả dụng"),
    GRD_007("GRD_007", "Lỗi phân tích phản hồi LLM"),
    GRD_008("GRD_008", "Dịch vụ báo cáo không khả dụng"),

    // Common errors
    COM_001("COM_001", "Dữ liệu không hợp lệ"),
    COM_002("COM_002", "Lỗi hệ thống"),
    COM_003("COM_003", "Yêu cầu không hợp lệ");

    private final String code;
    private final String message;
}

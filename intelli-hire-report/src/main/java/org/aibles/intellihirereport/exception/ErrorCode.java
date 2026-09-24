package org.aibles.intellihirereport.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Report errors
    RPT_001("RPT_001", "Không tìm thấy báo cáo"),
    RPT_002("RPT_002", "Báo cáo đã tồn tại cho phiên này"),
    RPT_003("RPT_003", "Mã phiên không hợp lệ"),
    RPT_004("RPT_004", "Trạng thái báo cáo không hợp lệ"),
    RPT_005("RPT_005", "Không có quyền truy cập, yêu cầu vai trò quản trị viên"),
    RPT_006("RPT_006", "Định dạng JSON lộ trình không hợp lệ"),
    RPT_007("RPT_007", "Không có quyền truy cập, bạn chỉ có thể truy cập dữ liệu của mình"),

    // Skill score errors
    SKL_001("SKL_001", "Không tìm thấy điểm kỹ năng"),

    // User progress errors
    PRG_001("PRG_001", "Không tìm thấy tiến độ người dùng"),

    // Common errors
    COM_001("COM_001", "Lỗi xác thực dữ liệu"),
    COM_002("COM_002", "Lỗi hệ thống"),
    COM_003("COM_003", "Yêu cầu không hợp lệ");

    private final String code;
    private final String message;
}

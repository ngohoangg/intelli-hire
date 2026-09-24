package org.aibles.intellihirequestion.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Question errors
    QUE_001("QUE_001", "Không tìm thấy câu hỏi"),
    QUE_002("QUE_002", "Câu hỏi đã tồn tại"),
    QUE_003("QUE_003", "Loại câu hỏi không hợp lệ"),

    // Topic errors
    TOP_001("TOP_001", "Không tìm thấy chủ đề"),
    TOP_002("TOP_002", "Chủ đề tham chiếu vòng lặp"),
    TOP_003("TOP_003", "Chủ đề đã tồn tại"),
    TOP_004("TOP_004", "Chủ đề đã bị khóa"),
    TOP_005("TOP_005", "Chủ đề có chủ đề con đang hoạt động"),
    TOP_006("TOP_006", "Chủ đề có câu hỏi đang hoạt động"),

    // Rubric errors
    RUB_001("RUB_001", "Không tìm thấy tiêu chí chấm điểm"),
    RUB_002("RUB_002", "Tiêu chí chấm điểm đã tồn tại cho câu hỏi này"),

    // Study article errors
    ART_001("ART_001", "Không tìm thấy bài viết học tập"),

    // Tag errors
    TAG_001("TAG_001", "Không tìm thấy nhãn"),
    TAG_002("TAG_002", "Nhãn đã tồn tại"),
    TAG_003("TAG_003", "Nhãn đang được sử dụng bởi câu hỏi"),
    TAG_004("TAG_004", "Nhãn đã bị khóa"),

    // Difficulty errors
    DIF_001("DIF_001", "Không tìm thấy độ khó"),
    DIF_002("DIF_002", "Độ khó đã tồn tại"),
    DIF_003("DIF_003", "Độ khó đang được sử dụng bởi câu hỏi hoặc bài viết học tập"),

    // Study article errors
    SA_001("SA_001", "Không tìm thấy bài viết học tập"),

    // Random question errors
    QUE_004("QUE_004", "Số lượng câu hỏi không hợp lệ, phải từ 1 đến 50"),
    QUE_005("QUE_005", "Không đủ câu hỏi khả dụng"),

    // Bulk import errors
    QUE_007("QUE_007", "Danh sách câu hỏi không được để trống"),
    QUE_008("QUE_008", "Danh sách câu hỏi không được vượt quá 100 mục"),
    QUE_009("QUE_009", "Yêu cầu tạo câu hỏi không hợp lệ"),

    // Resume client errors
    RSM_001("RSM_001", "Không tìm thấy hồ sơ"),

    // AI errors
    AI_001("AI_001", "Xác thực với nhà cung cấp AI thất bại"),
    AI_002("AI_002", "Yêu cầu tới nhà cung cấp AI thất bại"),

    // Common errors
    COM_001("COM_001", "Lỗi xác thực dữ liệu"),
    COM_002("COM_002", "Lỗi hệ thống"),
    COM_003("COM_003", "Yêu cầu không hợp lệ");

    private final String code;
    private final String message;
}

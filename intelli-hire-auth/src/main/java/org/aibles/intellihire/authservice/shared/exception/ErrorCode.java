package org.aibles.intellihire.authservice.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Platform errors (COM_*)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COM_001", "Dữ liệu gửi lên không hợp lệ"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COM_002", "Yêu cầu xác thực không hợp lệ hoặc thiếu thông tin"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COM_003", "Không có quyền truy cập tài nguyên này"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COM_004", "Không tìm thấy tài nguyên yêu cầu"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM_500", "Đã xảy ra lỗi hệ thống nội bộ"),

    // General errors
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GEN_9002", "Lỗi xác thực dữ liệu"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "GEN_9003", "Không có quyền truy cập"),

    // Authentication errors (1000-1099)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_1001", "Email/tên đăng nhập hoặc mật khẩu không đúng"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "AUTH_1002", "Tài khoản đã bị khóa. Vui lòng liên hệ hỗ trợ hoặc chờ mở khóa"),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "AUTH_1003", "Tài khoản không hoạt động"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_1004", "Email chưa được xác thực"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_1005", "Token không hợp lệ hoặc đã hết hạn"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_1006", "Không tìm thấy refresh token"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "AUTH_1007", "Refresh token đã bị thu hồi"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_1008", "Refresh token đã hết hạn"),
    SESSION_INACTIVE(HttpStatus.UNAUTHORIZED, "AUTH_1009", "Phiên đã hết hạn do không hoạt động"),

    // Registration errors (1100-1199)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_1101", "Tài khoản với email này đã tồn tại"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_1102", "Tên đăng nhập đã được sử dụng"),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_1103", "Mật khẩu không đáp ứng yêu cầu bảo mật"),

    // Password reset errors (1200-1299)
    PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_1201", "Token đặt lại mật khẩu đã hết hạn"),
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_1202", "Token đặt lại mật khẩu không hợp lệ"),
    PASSWORD_RESET_TOKEN_USED(HttpStatus.BAD_REQUEST, "AUTH_1203", "Token đặt lại mật khẩu đã được sử dụng"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_1204", "Mật khẩu mới và mật khẩu xác nhận không khớp"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_1205", "Mật khẩu mới phải khác với mật khẩu hiện tại"),

    // Email verification errors (1300-1399)
    EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_1301", "Token xác thực email đã hết hạn"),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_1302", "Token xác thực email không hợp lệ"),
    EMAIL_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_1303", "Email đã được xác thực"),

    // Account errors (2000-2099)
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_2001", "Không tìm thấy tài khoản"),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_2002", "Không tìm thấy hồ sơ người dùng"),

    // Role & Permission errors (3000-3099)
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "RBAC_3001", "Không tìm thấy vai trò"),
    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "RBAC_3002", "Không tìm thấy quyền"),
    ROLE_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "RBAC_3003", "Vai trò đã được gán cho tài khoản này"),
    CANNOT_DELETE_SYSTEM_ROLE(HttpStatus.BAD_REQUEST, "RBAC_3004", "Không thể xóa vai trò hệ thống"),

    // OAuth errors (4000-4099)
    OAUTH_PROVIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OAUTH_4001", "Không tìm thấy nhà cung cấp OAuth"),
    OAUTH_PROVIDER_DISABLED(HttpStatus.BAD_REQUEST, "OAUTH_4002", "Nhà cung cấp OAuth đã bị vô hiệu hóa"),
    OAUTH_LINK_ALREADY_EXISTS(HttpStatus.CONFLICT, "OAUTH_4003", "Nhà cung cấp OAuth đã được liên kết với tài khoản này"),
    OAUTH_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "OAUTH_4004", "Không tìm thấy liên kết OAuth"),
    OAUTH_CALLBACK_ERROR(HttpStatus.BAD_REQUEST, "OAUTH_4005", "Lỗi callback OAuth"),
    OAUTH_EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "OAUTH_4006", "Email từ nhà cung cấp OAuth đã được đăng ký"),

    // Rate limiting (5000-5099)
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RATE_5001", "Quá nhiều yêu cầu. Vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

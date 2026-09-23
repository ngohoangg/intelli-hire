package org.aibles.intellihire.authservice.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Authentication errors (1000-1099)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_1001", "Invalid email/username or password"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "AUTH_1002", "Account is locked. Please contact support or wait for unlock"),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "AUTH_1003", "Account is inactive"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_1004", "Email address is not verified"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_1005", "Token is invalid or expired"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_1006", "Refresh token not found"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "AUTH_1007", "Refresh token has been revoked"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_1008", "Refresh token has expired"),
    SESSION_INACTIVE(HttpStatus.UNAUTHORIZED, "AUTH_1009", "Session expired due to inactivity"),

    // Registration errors (1100-1199)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_1101", "An account with this email already exists"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_1102", "Username is already taken"),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_1103", "Password does not meet security requirements"),

    // Password reset errors (1200-1299)
    PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_1201", "Password reset token has expired"),
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_1202", "Password reset token is invalid"),
    PASSWORD_RESET_TOKEN_USED(HttpStatus.BAD_REQUEST, "AUTH_1203", "Password reset token has already been used"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_1204", "New password and confirm password do not match"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_1205", "New password must be different from the current password"),

    // Email verification errors (1300-1399)
    EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_1301", "Email verification token has expired"),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH_1302", "Email verification token is invalid"),
    EMAIL_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_1303", "Email is already verified"),

    // Account errors (2000-2099)
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_2001", "Account not found"),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_2002", "User profile not found"),

    // Role & Permission errors (3000-3099)
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "RBAC_3001", "Role not found"),
    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "RBAC_3002", "Permission not found"),
    ROLE_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "RBAC_3003", "Role is already assigned to this account"),
    CANNOT_DELETE_SYSTEM_ROLE(HttpStatus.BAD_REQUEST, "RBAC_3004", "Cannot delete system role"),

    // OAuth errors (4000-4099)
    OAUTH_PROVIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OAUTH_4001", "OAuth provider not found"),
    OAUTH_PROVIDER_DISABLED(HttpStatus.BAD_REQUEST, "OAUTH_4002", "OAuth provider is disabled"),
    OAUTH_LINK_ALREADY_EXISTS(HttpStatus.CONFLICT, "OAUTH_4003", "OAuth provider is already linked to this account"),
    OAUTH_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "OAUTH_4004", "OAuth link not found"),
    OAUTH_CALLBACK_ERROR(HttpStatus.BAD_REQUEST, "OAUTH_4005", "OAuth callback error"),
    OAUTH_EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "OAUTH_4006", "Email from OAuth provider is already registered"),

    // Rate limiting (5000-5099)
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RATE_5001", "Too many requests. Please try again later"),

    // General errors (9000-9099)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GEN_9001", "An unexpected error occurred"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GEN_9002", "Validation error"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "GEN_9003", "Access denied");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

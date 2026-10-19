package org.aibles.intellihire.authservice.auth.usecase.command;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank(message = "Email hoặc tên đăng nhập là bắt buộc")
        String identifier,

        @NotBlank(message = "Mật khẩu là bắt buộc")
        String password,

        String ipAddress,
        String userAgent
) {
}

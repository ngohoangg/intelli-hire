package org.aibles.intellihire.authservice.auth.usecase.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCommand(
        @NotBlank(message = "Email là bắt buộc")
        @Email(message = "Định dạng email không hợp lệ")
        String email,

        @NotBlank(message = "Tên đăng nhập là bắt buộc")
        @Size(min = 3, max = 50, message = "Tên đăng nhập phải có từ 3 đến 50 ký tự")
        String username,

        @NotBlank(message = "Mật khẩu là bắt buộc")
        @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
        String password,

        @NotBlank(message = "Mật khẩu xác nhận là bắt buộc")
        String confirmPassword,

        String firstName,
        String lastName,
        String ipAddress,
        String userAgent
) {
}

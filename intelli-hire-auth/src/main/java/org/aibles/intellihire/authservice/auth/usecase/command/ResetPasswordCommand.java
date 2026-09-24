package org.aibles.intellihire.authservice.auth.usecase.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordCommand(
        @NotBlank(message = "Token là bắt buộc")
        String token,

        @NotBlank(message = "Mật khẩu mới là bắt buộc")
        @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
        String newPassword
) {
}

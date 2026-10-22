package org.aibles.intellihire.authservice.auth.usecase.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordCommand(
        @NotBlank(message = "Email là bắt buộc")
        @Email(message = "Định dạng email không hợp lệ")
        String email,

        String ipAddress
) {
}

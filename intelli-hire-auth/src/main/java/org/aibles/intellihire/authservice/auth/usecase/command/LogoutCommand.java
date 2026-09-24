package org.aibles.intellihire.authservice.auth.usecase.command;

import jakarta.validation.constraints.NotBlank;

public record LogoutCommand(
        @NotBlank(message = "Refresh token là bắt buộc")
        String refreshToken
) {
}

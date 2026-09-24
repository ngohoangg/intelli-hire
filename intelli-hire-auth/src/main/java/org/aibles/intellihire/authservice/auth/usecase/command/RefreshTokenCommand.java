package org.aibles.intellihire.authservice.auth.usecase.command;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenCommand(
        @NotBlank(message = "Refresh token là bắt buộc")
        String refreshToken
) {
}

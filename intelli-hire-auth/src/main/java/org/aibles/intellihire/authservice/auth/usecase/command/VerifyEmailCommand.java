package org.aibles.intellihire.authservice.auth.usecase.command;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailCommand(
        @NotBlank(message = "Token là bắt buộc")
        String token
) {
}

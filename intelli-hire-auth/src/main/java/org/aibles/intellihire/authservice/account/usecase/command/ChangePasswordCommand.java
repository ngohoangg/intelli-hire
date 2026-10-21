package org.aibles.intellihire.authservice.account.usecase.command;

public record ChangePasswordCommand(
        Long accountId,
        String currentPassword,
        String newPassword,
        String confirmPassword
) {
}

package org.aibles.intellihire.authservice.admin.usecase.command;

import java.time.LocalDateTime;

public record AssignRoleCommand(
        Long accountId,
        Long roleId,
        LocalDateTime expiresAt
) {
}

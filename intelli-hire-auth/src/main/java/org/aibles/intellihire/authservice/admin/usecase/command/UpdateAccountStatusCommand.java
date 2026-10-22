package org.aibles.intellihire.authservice.admin.usecase.command;

import org.aibles.intellihire.authservice.account.domain.AccountStatus;

public record UpdateAccountStatusCommand(
        Long accountId,
        AccountStatus status,
        String reason
) {
}

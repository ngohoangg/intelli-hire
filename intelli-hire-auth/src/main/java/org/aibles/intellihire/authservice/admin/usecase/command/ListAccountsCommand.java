package org.aibles.intellihire.authservice.admin.usecase.command;

import org.aibles.intellihire.authservice.account.domain.AccountStatus;

public record ListAccountsCommand(
        int page,
        int size,
        AccountStatus status,
        String search
) {
}

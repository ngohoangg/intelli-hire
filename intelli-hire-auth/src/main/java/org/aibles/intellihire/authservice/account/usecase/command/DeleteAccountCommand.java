package org.aibles.intellihire.authservice.account.usecase.command;

public record DeleteAccountCommand(
        Long accountId,
        String password
) {
}

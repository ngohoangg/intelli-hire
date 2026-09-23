package org.aibles.intellihire.authservice.account.usecase;

import org.aibles.intellihire.authservice.account.usecase.command.DeleteAccountCommand;

public interface DeleteAccountUseCase {
    void execute(DeleteAccountCommand command);
}

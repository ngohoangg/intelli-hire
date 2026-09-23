package org.aibles.intellihire.authservice.account.usecase;

import org.aibles.intellihire.authservice.account.usecase.command.ChangePasswordCommand;

public interface ChangePasswordUseCase {
    void execute(ChangePasswordCommand command);
}

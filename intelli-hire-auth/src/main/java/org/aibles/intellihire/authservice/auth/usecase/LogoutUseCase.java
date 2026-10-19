package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.usecase.command.LogoutCommand;

public interface LogoutUseCase {
    void execute(LogoutCommand command);

    void logoutAll(Long accountId);
}

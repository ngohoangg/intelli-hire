package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.usecase.command.RegisterCommand;

public interface RegisterUseCase {
    RegisterResponse execute(RegisterCommand command);

    record RegisterResponse(
            Long accountId,
            String email,
            String username,
            String message
    ) {
    }
}

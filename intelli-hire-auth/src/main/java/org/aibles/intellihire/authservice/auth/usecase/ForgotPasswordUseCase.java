package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.usecase.command.ForgotPasswordCommand;

public interface ForgotPasswordUseCase {
    ForgotPasswordResponse execute(ForgotPasswordCommand command);

    record ForgotPasswordResponse(
            String message
    ) {
    }
}

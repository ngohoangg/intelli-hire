package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.usecase.command.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    ResetPasswordResponse execute(ResetPasswordCommand command);

    record ResetPasswordResponse(
            String message
    ) {
    }
}

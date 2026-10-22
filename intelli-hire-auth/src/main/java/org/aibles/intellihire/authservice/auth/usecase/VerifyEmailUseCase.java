package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.usecase.command.VerifyEmailCommand;

public interface VerifyEmailUseCase {
    VerifyEmailResponse execute(VerifyEmailCommand command);

    record VerifyEmailResponse(
            String message
    ) {
    }
}

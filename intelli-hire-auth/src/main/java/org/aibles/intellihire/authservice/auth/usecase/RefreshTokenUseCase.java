package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.usecase.command.RefreshTokenCommand;

public interface RefreshTokenUseCase {
    LoginUseCase.LoginResponse execute(RefreshTokenCommand command);
}

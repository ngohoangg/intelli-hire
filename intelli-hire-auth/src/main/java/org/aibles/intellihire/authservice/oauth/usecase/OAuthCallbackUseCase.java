package org.aibles.intellihire.authservice.oauth.usecase;

import org.aibles.intellihire.authservice.auth.usecase.LoginUseCase;
import org.aibles.intellihire.authservice.oauth.usecase.command.OAuthCallbackCommand;

public interface OAuthCallbackUseCase {
    LoginUseCase.LoginResponse execute(OAuthCallbackCommand command);
}

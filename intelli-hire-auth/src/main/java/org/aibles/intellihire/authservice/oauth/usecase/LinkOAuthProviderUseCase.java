package org.aibles.intellihire.authservice.oauth.usecase;

import org.aibles.intellihire.authservice.oauth.usecase.command.LinkOAuthProviderCommand;

public interface LinkOAuthProviderUseCase {
    void execute(LinkOAuthProviderCommand command);
}

package org.aibles.intellihire.authservice.oauth.usecase;

public interface GetOAuthAuthorizationUrlUseCase {
    String execute(String providerCode, String state);
}

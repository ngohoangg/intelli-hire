package org.aibles.intellihire.authservice.oauth.client;

import org.aibles.intellihire.authservice.oauth.domain.OAuthUserInfo;

public interface OAuthClient {

    String getProviderCode();

    String getAuthorizationUrl(String state);

    OAuthUserInfo exchangeCodeForUserInfo(String code);
}

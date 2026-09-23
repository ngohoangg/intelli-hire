package org.aibles.intellihire.authservice.oauth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class OAuthProviderNotFoundException extends BaseException {

    public OAuthProviderNotFoundException() {
        super(ErrorCode.OAUTH_PROVIDER_NOT_FOUND);
    }
}

package org.aibles.intellihire.authservice.oauth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class OAuthProviderDisabledException extends BaseException {

    public OAuthProviderDisabledException() {
        super(ErrorCode.OAUTH_PROVIDER_DISABLED);
    }
}

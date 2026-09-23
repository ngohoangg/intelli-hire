package org.aibles.intellihire.authservice.oauth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class OAuthEmailAlreadyRegisteredException extends BaseException {

    public OAuthEmailAlreadyRegisteredException() {
        super(ErrorCode.OAUTH_EMAIL_ALREADY_REGISTERED);
    }
}

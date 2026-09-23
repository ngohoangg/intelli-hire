package org.aibles.intellihire.authservice.oauth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class OAuthCallbackException extends BaseException {

    public OAuthCallbackException() {
        super(ErrorCode.OAUTH_CALLBACK_ERROR);
    }

    public OAuthCallbackException(String details) {
        super(ErrorCode.OAUTH_CALLBACK_ERROR, details);
    }
}

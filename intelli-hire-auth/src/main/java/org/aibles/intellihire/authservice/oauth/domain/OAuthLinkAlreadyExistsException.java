package org.aibles.intellihire.authservice.oauth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class OAuthLinkAlreadyExistsException extends BaseException {

    public OAuthLinkAlreadyExistsException() {
        super(ErrorCode.OAUTH_LINK_ALREADY_EXISTS);
    }
}

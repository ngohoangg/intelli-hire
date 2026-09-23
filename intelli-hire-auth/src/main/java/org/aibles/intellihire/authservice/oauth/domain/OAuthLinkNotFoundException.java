package org.aibles.intellihire.authservice.oauth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class OAuthLinkNotFoundException extends BaseException {

    public OAuthLinkNotFoundException() {
        super(ErrorCode.OAUTH_LINK_NOT_FOUND);
    }
}

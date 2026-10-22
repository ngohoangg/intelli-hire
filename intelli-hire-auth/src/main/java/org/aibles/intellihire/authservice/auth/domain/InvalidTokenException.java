package org.aibles.intellihire.authservice.auth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(String details) {
        super(ErrorCode.INVALID_TOKEN, details);
    }
}

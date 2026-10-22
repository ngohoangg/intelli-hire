package org.aibles.intellihire.authservice.auth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class AccountInactiveException extends BaseException {

    public AccountInactiveException() {
        super(ErrorCode.ACCOUNT_INACTIVE);
    }
}

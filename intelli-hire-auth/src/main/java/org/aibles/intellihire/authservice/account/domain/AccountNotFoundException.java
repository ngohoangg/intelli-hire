package org.aibles.intellihire.authservice.account.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class AccountNotFoundException extends BaseException {

    public AccountNotFoundException() {
        super(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    public AccountNotFoundException(String details) {
        super(ErrorCode.ACCOUNT_NOT_FOUND, details);
    }
}

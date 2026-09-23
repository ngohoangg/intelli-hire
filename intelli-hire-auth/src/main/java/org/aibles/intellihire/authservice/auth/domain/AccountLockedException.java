package org.aibles.intellihire.authservice.auth.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class AccountLockedException extends BaseException {

    public AccountLockedException() {
        super(ErrorCode.ACCOUNT_LOCKED);
    }

    public AccountLockedException(String reason) {
        super(ErrorCode.ACCOUNT_LOCKED, reason);
    }
}

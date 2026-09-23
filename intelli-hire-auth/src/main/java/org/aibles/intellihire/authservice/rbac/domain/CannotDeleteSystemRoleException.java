package org.aibles.intellihire.authservice.rbac.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class CannotDeleteSystemRoleException extends BaseException {

    public CannotDeleteSystemRoleException() {
        super(ErrorCode.CANNOT_DELETE_SYSTEM_ROLE);
    }
}

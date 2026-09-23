package org.aibles.intellihire.authservice.rbac.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class RoleNotFoundException extends BaseException {

    public RoleNotFoundException() {
        super(ErrorCode.ROLE_NOT_FOUND);
    }
}

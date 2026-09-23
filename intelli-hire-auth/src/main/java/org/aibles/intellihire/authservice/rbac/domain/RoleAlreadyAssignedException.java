package org.aibles.intellihire.authservice.rbac.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class RoleAlreadyAssignedException extends BaseException {

    public RoleAlreadyAssignedException() {
        super(ErrorCode.ROLE_ALREADY_ASSIGNED);
    }
}

package org.aibles.intellihire.authservice.rbac.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class PermissionNotFoundException extends BaseException {

    public PermissionNotFoundException() {
        super(ErrorCode.PERMISSION_NOT_FOUND);
    }
}

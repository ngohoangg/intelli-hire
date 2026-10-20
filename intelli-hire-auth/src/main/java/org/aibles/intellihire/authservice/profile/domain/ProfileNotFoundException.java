package org.aibles.intellihire.authservice.profile.domain;

import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;

public class ProfileNotFoundException extends BaseException {

    public ProfileNotFoundException() {
        super(ErrorCode.PROFILE_NOT_FOUND);
    }

    public ProfileNotFoundException(String details) {
        super(ErrorCode.PROFILE_NOT_FOUND, details);
    }

    public ProfileNotFoundException(Long accountId) {
        super(ErrorCode.PROFILE_NOT_FOUND, "Không tìm thấy hồ sơ cho tài khoản ID: " + accountId);
    }
}

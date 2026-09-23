package org.aibles.intellihiregrading.exception;

import org.springframework.http.HttpStatus;

public class DuplicateException extends BaseException {
    public DuplicateException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.CONFLICT);
    }
}

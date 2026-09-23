package org.aibles.intellihirequestion.exception;

import org.springframework.http.HttpStatus;

public class InactiveResourceException extends BaseException {
    public InactiveResourceException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.LOCKED);
    }

    public InactiveResourceException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.LOCKED);
    }
}

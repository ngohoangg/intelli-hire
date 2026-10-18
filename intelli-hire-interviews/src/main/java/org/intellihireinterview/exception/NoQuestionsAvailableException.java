package org.aibles.intellihireinterview.exception;

import org.springframework.http.HttpStatus;

public class NoQuestionsAvailableException extends BaseException {
    public NoQuestionsAvailableException() {
        super(ErrorCode.INT_004, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

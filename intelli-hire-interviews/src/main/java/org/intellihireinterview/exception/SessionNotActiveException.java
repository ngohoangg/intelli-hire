package org.aibles.intellihireinterview.exception;

import org.springframework.http.HttpStatus;

public class SessionNotActiveException extends BaseException {
    public SessionNotActiveException(Long sessionId) {
        super(ErrorCode.INT_002.getCode(),
              "Interview session is not active: " + sessionId,
              HttpStatus.CONFLICT);
    }
}

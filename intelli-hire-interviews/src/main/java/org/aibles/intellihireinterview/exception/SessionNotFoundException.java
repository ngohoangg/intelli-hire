package org.aibles.intellihireinterview.exception;

import org.springframework.http.HttpStatus;

public class SessionNotFoundException extends BaseException {
    public SessionNotFoundException(Long sessionId) {
        super(ErrorCode.INT_001.getCode(),
              "Không tìm thấy phiên phỏng vấn: " + sessionId,
              HttpStatus.NOT_FOUND);
    }
}

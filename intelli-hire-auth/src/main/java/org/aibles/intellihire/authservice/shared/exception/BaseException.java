package org.aibles.intellihire.authservice.shared.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public BaseException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""));
        this.errorCode = errorCode;
        this.details = details;
    }

    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public BaseException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""), cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}
